package com.example.dormconnectapp.ui.feed

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dormconnectapp.R
import com.example.dormconnectapp.databinding.FragmentFeedBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.example.dormconnectapp.data.local.FeedPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class Feedfragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: FeedAdapter
    private val feedList = mutableListOf<FeedPost>()
    private var selectedImageUri: Uri? = null
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            Toast.makeText(context, "Image selected", Toast.LENGTH_SHORT).show()
        }
    }
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            Toast.makeText(requireContext(), "Location permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient
    private var selectedDistanceKm: Double? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()

        binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadFeedPosts()

        binding.fabAddPost.setOnClickListener {
            showAddPostDialog()
        }

        Toast.makeText(requireContext(), "Feedfragment loaded", Toast.LENGTH_SHORT).show()

        Log.d("FABCheck", "FAB exists: ${binding.fabAddPost != null}")
        binding.fabAddPost.setBackgroundColor(Color.GREEN)

        val fineLocationGranted = ActivityCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ActivityCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted && !coarseLocationGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireContext())

        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                loadFeedPosts()  // Refresh feed
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }



    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun showAddPostDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_post, null)
        val etPostContent = dialogView.findViewById<EditText>(R.id.etPostContent)
        val btnSelectImage = dialogView.findViewById<Button>(R.id.btnSelectImage)

        selectedImageUri = null

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("New Post")
            .setView(dialogView)
            .setPositiveButton("Post") { _, _ ->
                val content = etPostContent.text.toString().trim()
                if (content.isNotEmpty()) {
                    val localPath = selectedImageUri?.let { saveImageLocally(it) }
                    savePostToFirestore(content, localPath)
                } else {
                    Toast.makeText(context, "Post cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveImageLocally(uri: Uri): String? {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val filename = "post_image_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, filename)
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }



    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun loadFeedPosts() {
        if (
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("feed_posts")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) {
                    Toast.makeText(context, "Error loading posts", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    val filteredList = mutableListOf<FeedPost>()

                    for (doc in snapshots) {
                        val post = doc.toObject(FeedPost::class.java)
                        post.postId = doc.id

                        val postLat = doc.getDouble("latitude")
                        val postLon = doc.getDouble("longitude")

                        val include = if (location != null && postLat != null && postLon != null) {
                            selectedDistanceKm == null || isWithinDistance(
                                location.latitude, location.longitude, postLat, postLon, selectedDistanceKm!!
                            )
                        } else true

                        if (include) filteredList.add(post)
                    }

                    val sorted = when (binding.sortSpinner.selectedItem.toString()) {
                        "Distance" -> filteredList.sortedBy {
                            if (it.latitude != null && it.longitude != null && location != null) {
                                calculateDistance(location.latitude, location.longitude, it.latitude!!, it.longitude!!)
                            } else Double.MAX_VALUE
                        }
                        else -> filteredList.sortedByDescending { it.timestamp?.toDate()?.time }
                    }

                    adapter = FeedAdapter(sorted, location?.latitude, location?.longitude)
                    binding.feedRecyclerView.adapter = adapter
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371.0 // Radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2).pow(2.0) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).pow(2.0)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }



    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun savePostToFirestore(content: String, imagePath: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val username = doc.getString("name") ?: "Anonymous"

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    val post = hashMapOf(
                        "username" to username,
                        "content" to content,
                        "profileImageUrl" to null,
                        "postImageUrl" to imagePath,
                        "timestamp" to FieldValue.serverTimestamp(),
                        "latitude" to location?.latitude,
                        "longitude" to location?.longitude
                    )

                    FirebaseFirestore.getInstance().collection("feed_posts")
                        .add(post)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Post added!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to add post", Toast.LENGTH_LONG).show()
                        }
                }
            }
    }




    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun uploadImageAndSavePost(content: String, imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("feed_images/${System.currentTimeMillis()}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener { uri ->
                savePostToFirestore(content, uri.toString())
            }
            .addOnFailureListener {
                Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isWithinDistance(
        userLat: Double,
        userLon: Double,
        postLat: Double,
        postLon: Double,
        maxDistanceKm: Double = 10.0
    ): Boolean {
        val earthRadius = 6371 // km

        val dLat = Math.toRadians(postLat - userLat)
        val dLon = Math.toRadians(postLon - userLon)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(postLat)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c

        return distance <= maxDistanceKm
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
