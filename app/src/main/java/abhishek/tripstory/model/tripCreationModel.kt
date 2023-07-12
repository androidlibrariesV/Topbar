package abhishek.tripstory.model

data class tripCreationModel(
    val userName: String? = "",
    val userEmail: String? = "",
    val userImage: String? = "",
    val tripId: String? = "",
    val tripName: String? = "",
    val destination: String? = "",
    val startDate: String? = "",
    val endDate: String? = "",
    val photo: String? = "",
    val description: String? = ""
)
