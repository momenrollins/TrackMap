package com.houseofdevelopment.gps.homemap.model

import java.io.Serializable

data class UpdatedUnitModel(val name : String,
                            val cls : Int,
                            val carId : Int,
                            val bact : Int) : Serializable