package com.houseofdevelopment.gps.vehicallist.model

import java.io.Serializable

class ItemGroupDataModelGps3 : Serializable {
    var group_id: String = ""
    var user_id: String = ""
    var group_name: String = ""
    var group_desc: String = ""
    var count = 0
    var isExpanded = false
    var isSelected = false
}