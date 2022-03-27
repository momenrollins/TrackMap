package com.trackmap.gps.utils

object Validation {

    /**
     * method is used for checking if string is empty or not.
     *
     * @param mString as String
     * @return boolean true if [mString] is notnull.
     */
    fun isNotNull(mString: String?): Boolean {
        return when {
            mString == null -> {
                false
            }
            mString.equals("", ignoreCase = true) -> {
                false
            }
            mString.equals("N/A", ignoreCase = true) -> {
                false
            }
            mString.equals("[]", ignoreCase = true) -> {
                false
            }
            mString.equals("null", ignoreCase = true) -> {
                false
            }
            else -> !mString.equals("{}", ignoreCase = true)
        }
    }
}
