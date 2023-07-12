package abhishek.tripstory

import android.content.Context
import android.content.Context.MODE_PRIVATE

object currentEmail {
    fun getEmail(context:Context):String?{
        val preferences= context.getSharedPreferences("userr", MODE_PRIVATE)
        val x=preferences.getString("emailmain","")
        return x
    }
}