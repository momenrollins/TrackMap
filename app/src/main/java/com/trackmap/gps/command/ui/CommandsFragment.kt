package com.trackmap.gps.command.ui


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.trackmap.gps.DataValues
import com.trackmap.gps.DataValues.apiKey
import com.trackmap.gps.R
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.command.adapter.CommandsAdapter
import com.trackmap.gps.command.adapter.CommandsAdapterGps3
import com.trackmap.gps.command.model.CommandDataGps3
import com.trackmap.gps.command.model.CommandResponseModel
import com.trackmap.gps.command.viewmodel.CommandsViewModel
import com.trackmap.gps.databinding.FragmentCommandsBinding
import com.trackmap.gps.network.client.ApiClient
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.utils.Constants
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.NetworkUtil
import com.trackmap.gps.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 */
class CommandsFragment : BaseFragment() {

    private lateinit var carId: String
    lateinit var binding: FragmentCommandsBinding
    lateinit var viewmodel: CommandsViewModel

    private lateinit var adapter: CommandsAdapter
    private lateinit var adapterGps3: CommandsAdapterGps3

    companion object {
        private const val TAG = "CommandsFragment"
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_commands, container, false
        )

        viewmodel = ViewModelProvider(this).get(CommandsViewModel::class.java)
        if (arguments != null) {
            carId = arguments!!.getString("carId").toString()
        }
        handleActionBarHidePlusIcon(R.string.commands)
        //initializeAdapter()
        binding.lifecycleOwner = this
        if (DataValues.serverData.contains("s3")) {
            viewmodel.getCommandsGps3()
            viewmodel.commDataModelGps3.observe(viewLifecycleOwner) {
                if (it?.data != null && it.data!!.isNotEmpty())
                    initializeAdapterGps3(it.data!!)
            }
        } else {
            viewmodel.getCommands(carId)
            viewmodel.commDataModel.observe(viewLifecycleOwner) {
                if (it?.items != null && it.items!!.cmds != null && it.items!!.cmds!!.isNotEmpty())
                    initializeAdapter(it.items!!.cmds)
            }
        }
        return binding.root
    }

    private fun initializeAdapter(cmds: List<CommandResponseModel.CMDS>?) {
        adapter = CommandsAdapter(cmds, object : CommandsAdapter.CommandClickInterface {
            override fun commandClick(model: CommandResponseModel.CMDS) {
                callApi(carId, model.n.toString(), model.p.toString())
            }
        })
        binding.commandList.adapter = adapter
    }

    private fun initializeAdapterGps3(cmds: List<CommandDataGps3>?) {
        adapterGps3 = CommandsAdapterGps3(cmds, object : CommandsAdapterGps3.CommandClickInterface {
            override fun commandClick(model: CommandDataGps3) {
                callApiGps3(carId, model.name, model.type, model.cmd)
            }
        })
        binding.commandList.adapter = adapterGps3
    }

    private fun callApi(itemId: String, commandName: String, params: String) {

        Utils.showProgressBar(requireContext())
        val viewModelJob = Job()
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        val client = ApiClient.getApiClient()
        coroutineScope.launch {
            val jsonObj = JSONObject()
            try {
                jsonObj.put("itemId", itemId)
                jsonObj.put("commandName", commandName)
                jsonObj.put("linkType", "")
                jsonObj.put("param", params)
                jsonObj.put("timeout", "1")
                jsonObj.put("flags", "0")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "unit/exec_cmd"
            body["params"] = jsonObj.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            DebugLog.d("RequestBody =$body")
            if (NetworkUtil.getConnectionStatus(requireContext()) == NetworkUtil.NOT_CONNECTED) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.internet_connection_slow_or_disconnected),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    client.getTemplateList(body)
                    Utils.hideProgressBar()

                } catch (error: Throwable) {
                    DebugLog.d("error" + error.printStackTrace())
                    Utils.hideProgressBar()
                }
            }
        }
    }

    private fun callApiGps3(imei: String, commandName: String, type: String, cmd: String) {

        Utils.showProgressBar(requireContext())
        val viewModelJob = Job()
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        val client = ApiClient.getApiClientAddress()
        coroutineScope.launch {
            if (NetworkUtil.getConnectionStatus(requireContext()) == NetworkUtil.NOT_CONNECTED) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.internet_connection_slow_or_disconnected),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.sendCmdGps3(
                        "user", apiKey, "OBJECT_CMD_GPRS," +
                                imei + ",\"" + commandName + "\"," +
                                type + ",\"" + cmd + "\""
                    )
                    response.enqueue(object : retrofit2.Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            Log.d(TAG, "onResponse: ${response.body()!!}")
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.d(TAG, "onFailure: ${t.message}")
                        }

                    })
                    Utils.hideProgressBar()

                } catch (error: Throwable) {
                    Log.d(TAG, "callApiGps3: error ${error.message}")
                    DebugLog.d("error" + error.printStackTrace())
                    Utils.hideProgressBar()
                }
            }
        }
    }

}
