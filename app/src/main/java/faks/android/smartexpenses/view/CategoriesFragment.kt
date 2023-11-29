package faks.android.smartexpenses.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.room.Room
import faks.android.smartexpenses.R
import faks.android.smartexpenses.databinding.FragmentCategoriesBinding
import faks.android.smartexpenses.model.Category
import faks.android.smartexpenses.model.CategoryDao
import faks.android.smartexpenses.model.SmartExpensesLocalDatabase

class CategoriesFragment : Fragment() {


    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!


    private var incomeCategoriesSelected : Boolean = false
    private var expenseCategoriesSelected : Boolean = true


    private lateinit var categoryDao: CategoryDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)


        val mainFloatingButton = binding.categoryFragmentMainFloatingButton
        val listIncomeCategoriesButton = binding.listIncomeCategoriesButton
        val listExpenseCategoriesButton = binding.listExpenseCategoriesButton


        val db = Room.databaseBuilder(
            requireContext(),
            SmartExpensesLocalDatabase::class.java, "main-db"
        ).allowMainThreadQueries().build()

        categoryDao = db.categoryDao()

        listCategoriesByType()





        mainFloatingButton.setOnClickListener {
            activity?.let {

                val builder = AlertDialog.Builder(it)
                val customLayout = inflater.inflate(R.layout.add_category_popup_window_layout, null)

                //category data
                val categoryName = customLayout.findViewById<EditText>(R.id.add_category_name_edit_text)

                builder.setView(customLayout)
                    .setPositiveButton("Add") { _, _ ->

                        val name = categoryName.text.toString()
                        var mismatchName = false
                        val categories = categoryDao.getAll()
                        for(category in categories){
                            if (name == category.name){

                                mismatchName = true
                                break

                            }
                        }

                        if(!mismatchName){
                            val type = getSelectedType()
                            val newCategory = Category( name,type,"icon")
                            categoryDao.insertAll(newCategory)
                            clearCategoriesFromLayout()
                            listCategoriesByType()
                        }else{
                            Toast.makeText(
                                    requireContext(),
                                    "Category with that name already exists",
                                    Toast.LENGTH_LONG
                                ).show()
                        }

                    }
                    .setNegativeButton("Exit") { _, _ -> }

                val dialog = builder.create()
                dialog.show()
            }
        }


        listExpenseCategoriesButton.setOnClickListener {

            clearCategoriesFromLayout()

            incomeCategoriesSelected = false
            expenseCategoriesSelected = true

            listCategoriesByType()

        }

        listIncomeCategoriesButton.setOnClickListener {

            clearCategoriesFromLayout()

            incomeCategoriesSelected = true
            expenseCategoriesSelected = false

            listCategoriesByType()

        }



        return binding.root

    }

    private fun getSelectedType() : String {

        return if (incomeCategoriesSelected)
            "Income"
        else
            "Expense"

    }


    private fun addCategoryBriefView(category: Category){

        // create a new category view dynamically
        val newCategoryView = requireActivity().layoutInflater.inflate(R.layout.category_brief_view, null)

        // set category name in view
        val categoryName = newCategoryView.findViewById<TextView>(R.id.category_name_text_view)
        categoryName.text = category.name

        val deleteCategoryImageView = newCategoryView.findViewById<ImageView>(R.id.delete_category_image_view)

        deleteCategoryImageView.setOnClickListener{

            categoryDao.delete(category)

            clearCategoriesFromLayout()
            listCategoriesByType()


        }

        binding.categoryLinearLayoutContainer.addView(newCategoryView)


    }

    private fun listCategoriesByType(){

        val categories: List<Category> = categoryDao.findByCategoryType(getSelectedType())

        for (category in categories){
            addCategoryBriefView(category)
        }


    }

    private fun clearCategoriesFromLayout(){

        val linearLayout = binding.categoryLinearLayoutContainer

        if(linearLayout.childCount > 0)
            linearLayout.removeAllViews()

    }









    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}