package com.example.khulisawallet

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.khulisawallet.data.*
import com.example.khulisawallet.utils.ImageUtils
import com.example.khulisawallet.viewmodel.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    // --- ViewModels ---
    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var categoryViewModel: CategoryViewModel

    // --- UI ---
    private lateinit var tabType: TabLayout
    private lateinit var etTitle: TextInputEditText
    private lateinit var etAmount: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etNote: TextInputEditText
    private lateinit var etInterval: TextInputEditText
    private lateinit var layoutInterval: TextInputLayout
    private lateinit var spinnerCategory: Spinner
    private lateinit var switchRecurring: SwitchMaterial
    private lateinit var ivReceipt: ImageView
    private lateinit var btnCamera: MaterialButton
    private lateinit var btnGallery: MaterialButton
    private lateinit var btnSave: MaterialButton

    // --- State ---
    private var selectedDate: Long = System.currentTimeMillis()
    private var currentPhotoPath: String? = null
    private var categories: List<Category> = emptyList()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // --- Camera Launcher ---
    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoPath != null) {
            ivReceipt.visibility = View.VISIBLE
            ivReceipt.setImageURI(Uri.parse(currentPhotoPath))
        }
    }

    // --- Gallery Launcher ---
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            currentPhotoPath = it.toString()
            ivReceipt.visibility = View.VISIBLE
            ivReceipt.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        bindViews()
        setupToolbar()
        setupViewModels()
        setupDatePicker()
        setupRecurringToggle()
        setupCameraButtons()
        setupSaveButton()
    }

    private fun bindViews() {
        tabType = findViewById(R.id.tab_type)
        etTitle = findViewById(R.id.et_title)
        etAmount = findViewById(R.id.et_amount)
        etDate = findViewById(R.id.et_date)
        etNote = findViewById(R.id.et_note)
        etInterval = findViewById(R.id.et_interval)
        layoutInterval = findViewById(R.id.layout_interval)
        spinnerCategory = findViewById(R.id.spinner_category)
        switchRecurring = findViewById(R.id.switch_recurring)
        ivReceipt = findViewById(R.id.iv_receipt)
        btnCamera = findViewById(R.id.btn_camera)
        btnGallery = findViewById(R.id.btn_gallery)
        btnSave = findViewById(R.id.btn_save)

        // Set today's date as default
        etDate.setText(dateFormat.format(Date(selectedDate)))
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupViewModels() {
        val prefs = getSharedPreferences("khulisa_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        val db = AppDatabase.getDatabase(this)

        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(ExpenseRepository(db.expenseDao()))
        )[ExpenseViewModel::class.java]
        expenseViewModel.setUser(userId)

        categoryViewModel = ViewModelProvider(
            this,
            CategoryViewModelFactory(CategoryRepository(db.categoryDao()))
        )[CategoryViewModel::class.java]

        // Observe categories and populate spinner
        categoryViewModel.allActiveCategories.observe(this) { cats ->
            categories = cats
            val names = cats.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter
        }

        // Observe save result
        expenseViewModel.expenseOpResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${it.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
                expenseViewModel.clearResult()
            }
        }
    }

    private fun setupDatePicker() {
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    cal.set(year, month, day)
                    selectedDate = cal.timeInMillis
                    etDate.setText(dateFormat.format(cal.time))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupRecurringToggle() {
        switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            layoutInterval.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun setupCameraButtons() {
        btnCamera.setOnClickListener {
            val photoFile = try {
                ImageUtils.createImageFile(this)
            } catch (e: Exception) {
                Toast.makeText(this, "Could not create image file", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentPhotoPath = photoFile.absolutePath
            val photoUri = ImageUtils.getFileUri(this, photoFile)
            takePhotoLauncher.launch(photoUri)
        }

        btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val amountStr = etAmount.text.toString().trim()
            val note = etNote.text.toString().trim().ifEmpty { null }
            val isRecurring = switchRecurring.isChecked
            val intervalDays = if (isRecurring) etInterval.text.toString().toIntOrNull() else null

            // --- Validation ---
            if (title.isEmpty()) {
                etTitle.error = "Title is required"
                return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                etAmount.error = "Amount is required"
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                etAmount.error = "Enter a valid amount"
                return@setOnClickListener
            }
            if (categories.isEmpty()) {
                Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isRecurring && intervalDays == null) {
                etInterval.error = "Enter interval in days"
                return@setOnClickListener
            }

            // --- Determine type from tab ---
            val type = if (tabType.selectedTabPosition == 0) CategoryType.EXPENSE
            else CategoryType.INCOME

            val selectedCategory = categories[spinnerCategory.selectedItemPosition]

            // --- Save ---
            expenseViewModel.addExpense(
                categoryId = selectedCategory.id,
                title = title,
                amount = amount,
                type = type,
                date = selectedDate,
                note = note,
                imagePath = currentPhotoPath,
                isRecurring = isRecurring,
                recurringIntervalDays = intervalDays
            )
        }
    }
}