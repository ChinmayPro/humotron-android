import com.google.gson.Gson
import com.humotron.app.domain.modal.response.WeatherOverviewResponse

fun main() {
    val json = """{"status":"success","is_weather_connected":true,"data":[{"key":"hrv_vs_temperature","title":"HRV vs Temperature","subTitle":"Heat May Be Draining Your Recovery","description":"Explore how warmer nights might be stressing your system and slowing recovery.","is_eligible":true,"is_min_data":false,"recent_reports":[{"_id":"69ea0e4c24527282d30d6f47","selectedDate":"2026-04-09","title":"HRV vs Temperature","headline":"Cooler Days Boost Your Recovery","validDayCount":24,"createdAt":"2026-04-09T09:17:38.018Z"}]}]}"""
    val response = Gson().fromJson(json, WeatherOverviewResponse::class.java)
    println(response.data?.get(0)?.recentReports?.size)
    println(response.data?.get(0)?.isEligible)
}
