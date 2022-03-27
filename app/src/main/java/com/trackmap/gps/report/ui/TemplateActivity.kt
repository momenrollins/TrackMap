package com.trackmap.gps.report.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.trackmap.gps.R
import com.trackmap.gps.base.BaseActivity
import com.trackmap.gps.report.model.TempData
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.report.model.RepModel
import com.trackmap.gps.vehicallist.viewmodel.VehiclesListViewModel
import kotlinx.android.synthetic.main.template_layout_row_right.view.*
import kotlin.collections.ArrayList


class TemplateActivity : BaseActivity() {

    lateinit var listView: ListView
    lateinit var rvList: RecyclerView
    lateinit var toolbarTitle: TextView
    lateinit var imageBack: ImageView
    var arrayAdapter: ArrayAdapter<String>? = null
    var arrayAdapterRepModel: ArrayAdapter<RepModel>? = null
    var arrayAdapterRepModelGps3: ArrayAdapter<TempData>? = null
    var typeArray: List<String>? = null
    lateinit var viewModel: VehiclesListViewModel

    lateinit var repModelList: ArrayList<RepModel>
    lateinit var repModelListGps3: ArrayList<TempData>
    var selectedType = ""
    var isTemplate: Boolean = false
    private var serverData: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_template_list)
        serverData = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "").toString()

        initElements()
        getIntentData()
        initListener()
    }

    private fun initElements() {
        viewModel = ViewModelProviders.of(this).get(VehiclesListViewModel::class.java)
        listView = findViewById(R.id.listView)
        rvList = findViewById(R.id.rvList)
        toolbarTitle = findViewById(R.id.toolbar_title)
        listView.visibility = View.VISIBLE
        rvList.visibility = View.GONE
//        imageBack = findViewById(R.id.img_back)
//        if (Locale.getDefault().getDisplayLanguage().equals("العربية")) imageBack.rotation=180F

    }

    private val TAG = "TemplateActivity"
    private fun getIntentData() {
        val list_type = intent.getStringExtra("cate_name")
        when {
            list_type.equals("type") -> {
                typeArray = resources.getStringArray(R.array.type_array).toList()
                setListViewData()

            }
            /* list_type.equals("type_map") -> {
                 typeArray = resources.getStringArray(R.array.type_array_map).toList()
                 Log.d("array list", ":" + typeArray?.size)

                 setListViewData()

             }*/
            list_type.equals("template") -> {
                isTemplate = true
                selectedType = intent.getStringExtra("selectedItem")!!
                if (serverData.contains("s3")) {
                    repModelListGps3 =
                        intent.getSerializableExtra("repModelData") as ArrayList<TempData>
                } else {
                    repModelList =
                        intent.getParcelableArrayListExtra<Parcelable?>("repModelData") as ArrayList<RepModel>
                }
                getSeparateDataList(selectedType)

//            addObserver()
            }
            list_type.equals("type_map") -> {
                listView.visibility = View.GONE
                toolbarTitle.text = getString(R.string.map_type)
                rvList.visibility = View.VISIBLE
                typeArray = resources.getStringArray(R.array.type_array_map).toList()
                rvList.adapter = MapAdapter(typeArray as ArrayList<String>, this)
            }
        }
    }

    private fun getSeparateDataList(value: String) {
        val nameList = mutableListOf<RepModel>()
        val nameListGps3 = mutableListOf<TempData>()
        if (serverData.contains("s3")) {
            for (md in repModelListGps3) {
                if (value.equals(getString(R.string.`object`), true)) {
                    if (md.category.equals("Object")) {
                        nameListGps3.add(md)
                    }
                } else if (value.equals(getString(R.string.group), true)) {
                    if (md.category.equals("Group")) {
                        nameListGps3.add(md)
                    }
                }
            }
            arrayAdapterRepModelGps3 = ArrayAdapter<TempData>(
                this, R.layout.template_layout_row,
                R.id.textView, nameListGps3
            )
            listView.adapter = arrayAdapterRepModelGps3
        } else {
            for (md in repModelList) {
                if (value.equals(getString(R.string.`object`), true)) {
                    if (md.ct.equals("avl_unit")) {
                        nameList.add(md)
                    }
                } else if (value.equals(getString(R.string.group), true)) {
                    if (md.ct.equals("avl_unit_group")) {
                        nameList.add(md)
                    }
                }
            }
            arrayAdapterRepModel = ArrayAdapter<RepModel>(
                this, R.layout.template_layout_row,
                R.id.textView, nameList
            )
            listView.adapter = arrayAdapterRepModel
        }


    }

    private fun setListViewData() {
        arrayAdapter = ArrayAdapter<String>(
            this, android.R.layout.simple_list_item_1,
            android.R.id.text1, typeArray!!
        )
        listView.adapter = arrayAdapter

    }

    private fun initListener() {
        var selObjectId = ""
        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent()
            if (serverData.contains("s3")) {
                if (parent.getItemAtPosition(position) is String) {
                    val selectedItem = parent.getItemAtPosition(position) as String
                    intent.putExtra("result_from", "type_result")
                    intent.putExtra("selectedItem", selectedItem)
                    intent.putExtra("position", (position + 1))
                    Log.d(TAG, "initListener:1 $selectedItem")
                    if (isTemplate) {
                        for (rp in repModelList) {
                            if (rp.n.equals(selectedItem)) {
                                selObjectId = rp.id.toString()
                            }
                        }
                        intent.putExtra("objectId", selObjectId)
                    }
                } else if (parent.getItemAtPosition(position) is TempData) {
                    val selectedItem = parent.getItemAtPosition(position) as TempData
                    intent.putExtra("result_from", "type_result")
                    intent.putExtra("selectedItem", selectedItem.name)
                    intent.putExtra("position", (position + 1))
                    Log.d(TAG, "initListener:2 $selectedItem")
                    if (isTemplate) {
                        selObjectId = selectedItem.report_id.toString()
                        intent.putExtra("objectId", selObjectId)
                    }
                }
            } else {
                if (parent.getItemAtPosition(position) is String) {
                    val selectedItem = parent.getItemAtPosition(position) as String
                    intent.putExtra("result_from", "type_result")
                    intent.putExtra("selectedItem", selectedItem)
                    intent.putExtra("position", (position + 1))
                    Log.d(TAG, "initListener:3 $selectedItem")
                    if (isTemplate) {
                        for (rp in repModelList) {
                            if (rp.n.equals(selectedItem)) {
                                selObjectId = rp.id.toString()
                            }
                        }
                        intent.putExtra("objectId", selObjectId)
                    }
                } else if (parent.getItemAtPosition(position) is RepModel) {
                    val selectedItem = parent.getItemAtPosition(position) as RepModel
                    intent.putExtra("result_from", "type_result")
                    intent.putExtra("selectedItem", selectedItem.n)
                    intent.putExtra("position", (position + 1))
                    if (isTemplate) {
                        selObjectId = selectedItem.id.toString()
                        intent.putExtra("objectId", selObjectId)
                    }
                }
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}

class MapAdapter(private var mapList: ArrayList<String>, private var mContext: Context) :
    RecyclerView.Adapter<MapAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.template_layout_row_right, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mapList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            mapList[position],
            (MyPreference.getValueInt(PrefKey.MAP_TYPE, 1) - 1) == position
        )
        holder.itemView.mainData.tag = position
        holder.itemView.mainData.setOnClickListener {
            val intent = Intent()
            intent.putExtra("result_from", "type_result")
            intent.putExtra("selectedItem", mapList[it.tag as Int])
            intent.putExtra("position", (it.tag as Int) + 1)
            (mContext as TemplateActivity).setResult(Activity.RESULT_OK, intent)
            (mContext as TemplateActivity).finish()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(value: String, isvisible: Boolean) {

            itemView.textView.text = value
            if (isvisible)
                itemView.selectValues.visibility = View.VISIBLE
            else
                itemView.selectValues.visibility = View.GONE
        }
    }
}