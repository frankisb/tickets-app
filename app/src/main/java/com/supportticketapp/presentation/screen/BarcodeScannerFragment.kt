package com.supportticketapp.presentation.screen

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.supportticketapp.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScannerFragment : Fragment() {

    private var cameraExecutor: ExecutorService? = null
    private var handledResult = false

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_barcode_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        view.findViewById<ImageButton>(R.id.closeButton).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        requestCameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val context = requireContext()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val previewView = requireView().findViewById<PreviewView>(R.id.previewView)

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(
                cameraExecutor!!,
                BarcodeAnalyzer(
                    onBarcode = { rawValue ->
                        if (handledResult) return@BarcodeAnalyzer
                        handledResult = true

                        parentFragmentManager.setFragmentResult(
                            REQUEST_KEY,
                            Bundle().apply { putString(BUNDLE_KEY_RAW_VALUE, rawValue) }
                        )
                        parentFragmentManager.popBackStack()
                    },
                    onError = { e ->
                        Log.e("BarcodeScannerFragment", "Analyzer error", e)
                    }
                )
            )

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                analysis
            )
        }, ContextCompat.getMainExecutor(context))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor?.shutdown()
        cameraExecutor = null
    }

    private class BarcodeAnalyzer(
        private val onBarcode: (String) -> Unit,
        private val onError: (Exception) -> Unit
    ) : ImageAnalysis.Analyzer {

        private val scanner = BarcodeScanning.getClient()

        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                imageProxy.close()
                return
            }

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val rawValue = barcodes.firstOrNull()?.rawValue
                    if (!rawValue.isNullOrBlank()) {
                        onBarcode(rawValue)
                    }
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    companion object {
        const val REQUEST_KEY = "barcode_scan_result"
        const val BUNDLE_KEY_RAW_VALUE = "raw_value"
    }
}
