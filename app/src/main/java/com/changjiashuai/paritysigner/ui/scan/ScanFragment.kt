package com.changjiashuai.paritysigner.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.changjiashuai.paritysigner.*
import com.changjiashuai.paritysigner.databinding.FragmentScanBinding
import com.changjiashuai.paritysigner.viewmodel.CameraXViewModel
import com.changjiashuai.paritysigner.viewmodel.ScanViewModel
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.ScreenData

class ScanFragment : BaseFragment() {

    private val scanViewModel by viewModels<ScanViewModel>()
    private val cameraXViewModel by viewModels<CameraXViewModel>()
    private var _binding: FragmentScanBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var previewView: PreviewView? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var imageProcessor: VisionImageProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var cameraSelector: CameraSelector? = null

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    private val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(options)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        if (allPermissionsGranted()) {
            setupViewModel()
        } else {
            val launcherPermission =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    if (it) {
                        //同意
                        setupViewModel()
                    } else {
                        //拒绝
                        Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            launcherPermission.launch(REQUIRED_CAMERA_PERMISSIONS)
        }
    }

    override fun onResume() {
        super.onResume()
        scanViewModel.doAction(Action.NAVBAR_SCAN)
        hasProcessed = false
    }

    private fun allPermissionsGranted() = context?.let {
        ContextCompat.checkSelfPermission(it, REQUIRED_CAMERA_PERMISSIONS)
    } == PackageManager.PERMISSION_GRANTED


    private fun setupView() {
        Log.d(TAG, "setupView")
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        previewView = binding.previewView
        if (previewView == null) {
            Log.d(TAG, "previewView is null")
        }
        graphicOverlay = binding.graphicOverlay
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null")
        }
        binding.rlQrParse.btnStartOver.setOnClickListener {
            scanViewModel.resetScanValues()
            scanViewModel.doAction(Action.GO_BACK)
            hasProcessed = false
        }
    }

    private fun setupViewModel() {
        cameraXViewModel.processCameraProvider.observe(viewLifecycleOwner) {
            cameraProvider = it
            bindAllCameraUseCases()
        }
        scanViewModel.actionResult.observe(viewLifecycleOwner) {
            processActionResult(it)
        }
        scanViewModel.progress.observe(viewLifecycleOwner) {
            Log.i(TAG, "progress=${it * 100}")
            with(binding.rlQrParse.sbProgress) {
                if (progress != max) {
                    progress = (it * 100).toInt()
                }
            }
        }
        scanViewModel.captured.observe(viewLifecycleOwner) {
            Log.i(TAG, "captured=$it")
            binding.rlQrParse.tvFrames.text =
                "From $it / ${scanViewModel.total.value} captured frames"
        }
        scanViewModel.total.observe(viewLifecycleOwner) {
            Log.i(TAG, "total=$it")
            if (it == null){
                binding.rlQrParse.sbProgress.progress = 0
            }
        }
    }

    private fun bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider!!.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    private fun bindPreviewUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        val builder = Preview.Builder()
//        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing)
//        if (targetResolution != null) {
//            builder.setTargetResolution(targetResolution)
//        }
        previewUseCase = builder.build()
        previewUseCase!!.setSurfaceProvider(previewView!!.surfaceProvider)
        cameraProvider!!.bindToLifecycle(/* lifecycleOwner= */ this,
            cameraSelector!!,
            previewUseCase
        )
    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }
        if (imageProcessor != null) {
            imageProcessor!!.stop()
        }
        imageProcessor = context?.let { BarcodeScannerProcessor(it) }

        val builder = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing)
//        if (targetResolution != null) {
//            builder.setTargetResolution(targetResolution)
//        }
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        context?.let {
//            analysisUseCase?.setAnalyzer(
//                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
//                // thus we can just runs the analyzer itself on main thread.
//                ContextCompat.getMainExecutor(it)
//            ) { imageProxy: ImageProxy ->
//                if (needUpdateGraphicOverlayImageSourceInfo) {
//                    val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
//                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
//                    if (rotationDegrees == 0 || rotationDegrees == 180) {
//                        graphicOverlay!!.setImageSourceInfo(
//                            imageProxy.width,
//                            imageProxy.height,
//                            isImageFlipped
//                        )
//                    } else {
//                        graphicOverlay!!.setImageSourceInfo(
//                            imageProxy.height,
//                            imageProxy.width,
//                            isImageFlipped
//                        )
//                    }
//                    needUpdateGraphicOverlayImageSourceInfo = false
//                }
//                try {
//                    imageProcessor!!.processImageProxy(imageProxy, graphicOverlay)
//                } catch (e: MlKitException) {
//                    Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)
//                    Toast.makeText(
//                        context,
//                        e.localizedMessage,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
            analysisUseCase?.setAnalyzer(ContextCompat.getMainExecutor(it)) { imageProxy ->
                scanViewModel.processFrame(barcodeScanner, imageProxy)
            }
        }
        cameraProvider!!.bindToLifecycle(/* lifecycleOwner= */ this,
            cameraSelector!!,
            analysisUseCase
        )
    }

    private var hasProcessed = false

    override fun processScreenData(screenData: ScreenData) {
        when (screenData) {
            //Transaction
            is ScreenData.Transaction -> {
                if (!hasProcessed) {
                    hasProcessed = true
                    TransactionPreviewActivity.mTransaction = screenData.f
                    context?.let {
                        TransactionPreviewActivity.startActivity(it)
                    }
                }
            }
            else -> {

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ScanFragment"
        private const val REQUIRED_CAMERA_PERMISSIONS = Manifest.permission.CAMERA
    }
}