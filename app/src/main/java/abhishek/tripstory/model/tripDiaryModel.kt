package abhishek.tripstory.model

data class tripDiaryModel (

        val userName: String?= "" ,
        val userEmail: String?= "" ,
        val userImage: String?= "" ,
        val tripId: String?= "" ,
        val tripName: String?= "" ,
        val diaryName: String?= "" ,
        val date: String?= "" ,
        val imageList: ArrayList<String>?= ArrayList() ,
        val description: String?= "" ,
        val location: String?= "",
        var expand : Boolean = false

)