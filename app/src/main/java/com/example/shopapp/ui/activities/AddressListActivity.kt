package com.example.shopapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopapp.R
import com.example.shopapp.firestore.FirestoreClass
import com.example.shopapp.models.Address
import com.example.shopapp.ui.adapters.AddressListAdapter
import com.example.shopapp.utils.Constants
import com.example.shopapp.utils.SwipeToDeleteCallback
import com.example.shopapp.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_address_list.*

/**
 * Address list screen.
 */
class AddressListActivity : BaseActivity() {
    private var mSelectAddress: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_list)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        if (intent.hasExtra(Constants.EXTRA_SELECT_ADDRESS)) {
            mSelectAddress =
                intent.getBooleanExtra(Constants.EXTRA_SELECT_ADDRESS, false)
        }

        setupActionBar()

        if (mSelectAddress) {
            tv_title.text = resources.getString(R.string.title_select_address)
        }

        tv_add_address.setOnClickListener {
            val intent = Intent(this@AddressListActivity, AddEditAddressActivity::class.java)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, Constants.ADD_ADDRESS_REQUEST_CODE)


        }
        getAddressList()
    }

    /**
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.ADD_ADDRESS_REQUEST_CODE) {
                getAddressList()
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Request Cancelled", "To add the address.")
        }
    }


    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbar_address_list_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_address_list_activity.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to get the list of address from cloud Firestore.
     */
    private fun getAddressList() {
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getAddressesList(this@AddressListActivity)
    }

    /**
     * A function to get the success result of address list from cloud Firestore.
     *
     * @param addressList
     */
    fun successAddressListFromFirestore(addressList: ArrayList<Address>) {
        hideProgressDialog()

        if (addressList.size > 0) {

            rv_address_list.visibility = View.VISIBLE
            tv_no_address_found.visibility = View.GONE

            rv_address_list.layoutManager = LinearLayoutManager(this@AddressListActivity)
            rv_address_list.setHasFixedSize(true)

            val addressAdapter =
                AddressListAdapter(this@AddressListActivity, addressList, mSelectAddress)
            rv_address_list.adapter = addressAdapter

            if (!mSelectAddress) {
                val editSwipeHandler = object : SwipeToEditCallback(this) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val adapter = rv_address_list.adapter as AddressListAdapter
                        adapter.notifyEditItem(
                            this@AddressListActivity,
                            viewHolder.adapterPosition
                        )
                    }
                }
                val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
                editItemTouchHelper.attachToRecyclerView(rv_address_list)


                val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        showProgressDialog(resources.getString(R.string.please_wait))

                        FirestoreClass().deleteAddress(
                            this@AddressListActivity,
                            addressList[viewHolder.adapterPosition].id
                        )
                    }
                }
                val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
                deleteItemTouchHelper.attachToRecyclerView(rv_address_list)
            }
        } else {
            rv_address_list.visibility = View.GONE
            tv_no_address_found.visibility = View.VISIBLE
        }
    }

    /**
     * A function notify the user that the address is deleted successfully.
     */
    fun deleteAddressSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this@AddressListActivity,
            resources.getString(R.string.err_your_address_deleted_successfully),
            Toast.LENGTH_SHORT
        ).show()
        getAddressList()
    }
}