import Foundation

struct WeatherDay: Codable {
    val date: String
    val maxTemp: Int
    val minTemp: Int
    val code: Int
}

struct WeatherForecast: Codable {
    val summary: WeatherDay
    val details: [WeatherDay?]
}

struct OpenMeteoResponse: Codable {
    val daily: OpenMeteoDaily
}

struct OpenMeteoDaily: Codable {
    val time: [String]
    val weather_code: [Int]
    val temperature_2m_max: [Double]
    val temperature_2m_min: [Double]
}

class WeatherService {
    private let lat = 41.2856
    private let lng = 1.2504
    
    func getWeekendWeather(fridayDateStr: String) async -> WeatherForecast? {
        let urlString = "https://api.open-meteo.com/v1/forecast?latitude=\(lat)&longitude=\(lng)&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto"
        
        guard let url = URL(string: urlString) else { return nil }
        
        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            let response = try JSONDecoder().decode(OpenMeteoResponse.self, from: data)
            
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd"
            
            guard let friday = dateFormatter.date(from: fridayDateStr) else { return nil }
            let dates = [
                fridayDateStr,
                dateFormatter.string(from: Calendar.current.date(byAdding: .day, value: 1, to: friday)!),
                dateFormatter.string(from: Calendar.current.date(byAdding: .day, value: 2, to: friday)!)
            ]
            
            let weekendForecast = dates.map { date -> WeatherDay? in
                guard let dayIndex = response.daily.time.firstIndex(of: date) else { return nil }
                return WeatherDay(
                    date: date,
                    maxTemp: Int(response.daily.temperature_2m_max[dayIndex]),
                    minTemp: Int(response.daily.temperature_2m_min[dayIndex]),
                    code: response.daily.weather_code[dayIndex]
                )
            }
            
            if weekendForecast.compactMap({ $0 }).isEmpty { return nil }
            
            guard let summary = weekendForecast[1] ?? weekendForecast.compactMap({ $0 }).first else { return nil }
            
            return WeatherForecast(summary: summary, details: weekendForecast)
        } catch {
            print("Weather error: \(error)")
            return nil
        }
    }
}
