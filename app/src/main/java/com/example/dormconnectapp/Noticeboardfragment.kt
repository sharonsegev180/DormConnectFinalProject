package com.example.dormconnectapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dormconnectapp.databinding.FragmentNoticeBoardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

class NoticeBoardFragment : Fragment() {

    private var _binding: FragmentNoticeBoardBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var selectedImageUri: Uri? = null

    // Image picker launcher
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                Toast.makeText(context, "Image selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeBoardBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.fabAddNote.setOnClickListener {
            showAddNoteDialog()
        }
        loadNotes()
        return binding.root
    }

    private fun showAddNoteDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_note, null)
        val etNoteContent = dialogView.findViewById<EditText>(R.id.etNoteContent)
        val btnSelectImage = dialogView.findViewById<View>(R.id.btnSelectImage)

        selectedImageUri = null

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add a Note")
            .setView(dialogView)
            .setPositiveButton("Post") { _, _ ->
                val content = etNoteContent.text.toString().trim()
                if (content.isNotEmpty()) {
                    val imagePath = selectedImageUri?.let { saveImageLocally(it) }
                    saveNoteToFirestore(content, imagePath)
                } else {
                    Toast.makeText(context, "Note cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }

            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadNotes() {
        firestore.collection("notice_board")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                binding.notesContainer.removeAllViews()

                for (doc in snapshots) {
                    val noteView = layoutInflater.inflate(R.layout.item_note, binding.notesContainer, false)

                    val noteText = noteView.findViewById<TextView>(R.id.noteContentText)
                    val noteImage = noteView.findViewById<ImageView>(R.id.noteImageView)
                    val authorText = noteView.findViewById<TextView>(R.id.noteAuthorText) // ← Add this line

                    noteText.text = doc.getString("content") ?: ""

                    val author = doc.getString("author") ?: "Anonymous"
                    authorText.text = "by $author" // ← Show author

                    val imagePath = doc.getString("imagePath")
                    if (!imagePath.isNullOrEmpty()) {
                        noteImage.visibility = View.VISIBLE
                        Glide.with(this)
                            .load(File(imagePath))
                            .into(noteImage)
                    } else {
                        noteImage.visibility = View.GONE
                    }

                    binding.notesContainer.addView(noteView)
                }
            }
    }



    private fun saveImageLocally(uri: Uri): String? {
        val context = requireContext()
        val inputStream = context.contentResolver.openInputStream(uri)
        val filename = "note_image_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }


    private fun saveNoteToFirestore(noteContent: String, localImagePath: String?) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val username = doc.getString("name") ?: "Anonymous"

                val note = hashMapOf(
                    "content" to noteContent,
                    "author" to username,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "imagePath" to localImagePath
                )

                firestore.collection("notice_board")
                    .add(note)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Note added!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to save note", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
            }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
