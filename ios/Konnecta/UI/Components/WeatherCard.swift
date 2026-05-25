import SwiftUI

struct WeatherCard: View {
    let weather: WeatherDay?
    
    var body: some View {
        if let weather = weather {
            HStack {
                HStack(spacing: 16) {
                    Text(getWeatherIcon(code: weather.code))
                        .font(.system(size: 32))
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("VALLS")
                            .font(.system(size: 10, weight: .black))
                            .kerning(2)
                            .foregroundColor(.secondary)
                        
                        Text(getWeatherDescription(code: weather.code))
                            .font(.system(size: 16, weight: .bold))
                    }
                }
                
                Spacer()
                
                VStack(alignment: .trailing, spacing: 0) {
                    Text("\(weather.maxTemp)°")
                        .font(.system(size: 24, weight: .black))
                    
                    Text("\(weather.minTemp)°")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.secondary)
                }
            }
            .padding(24)
            .background(Color(.systemGray6))
            .cornerRadius(32)
            .padding(.horizontal, 16)
        } else {
            HStack {
                Spacer()
                Text("MASSA AVIAT PER PREDIR EL TEMPS")
                    .font(.system(size: 10, weight: .black))
                    .kerning(2)
                    .foregroundColor(.secondary.opacity(0.5))
                Spacer()
            }
            .padding(24)
            .background(Color(.systemGray6).opacity(0.5))
            .cornerRadius(32)
            .padding(.horizontal, 16)
        }
    }
    
    func getWeatherIcon(code: Int) -> String {
        switch code {
        case 0...1: return "☀️"
        case 2: return "⛅"
        case 3: return "☁️"
        case 4...48: return "🌫️"
        case 49...67: return "🌧️"
        case 68...77: return "❄️"
        case 78...82: return "🌦️"
        default: return "⛈️"
        }
    }
    
    func getWeatherDescription(code: Int) -> String {
        switch code {
        case 0: return "Sol radiant"
        case 1...2: return "Cel clar"
        case 3: return "Nuvolat"
        case 45, 48: return "Boira"
        case 51...55: return "Plugim suau"
        case 61...67: return "Pluja"
        case 71...77: return "Neu"
        case 80...82: return "Ruixats"
        default: return "Tempesta"
        }
    }
}
