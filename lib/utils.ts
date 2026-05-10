import {
  nextFriday,
  isFriday,
  startOfDay,
  addWeeks,
  format,
  addDays,
  parseISO,
  isSameDay,
  isSaturday,
  isSunday,
  subDays,
} from "date-fns";
import { ca } from "date-fns/locale";

export const HOMETOWN_COORDINATES = { lat: 41.2856, lng: 1.2504 };

export function getUpcomingFriday() {
  const now = new Date();
  if (isFriday(now)) return startOfDay(now);
  if (isSaturday(now)) return startOfDay(subDays(now, 1));
  if (isSunday(now)) return startOfDay(subDays(now, 2));
  return nextFriday(now);
}

export function isUpcomingWeekend(weekendDate: string) {
  const upcomingFriday = getUpcomingFriday();
  const anchorDate = parseISO(weekendDate);
  return isSameDay(anchorDate, upcomingFriday);
}

export function getFormattedDayText(weekendDate: string, dayOfWeek: string) {
  const anchorDate = parseISO(weekendDate);
  const isUpcoming = isUpcomingWeekend(weekendDate);

  // Calculem la data real de l'esdeveniment basat en el dia triat
  let eventDate = anchorDate;
  if (dayOfWeek === "dissabte") eventDate = addDays(anchorDate, 1);
  if (dayOfWeek === "diumenge") eventDate = addDays(anchorDate, 2);

  if (isUpcoming) {
    return `aquest ${dayOfWeek}`;
  } else {
    return `${dayOfWeek} ${format(eventDate, "d 'de' MMMM", { locale: ca })}`;
  }
}

export function getNextWeekends(count = 10) {
  const firstFriday = getUpcomingFriday();
  return Array.from({ length: count }).map((_, i) => addWeeks(firstFriday, i));
}

export function formatDbDate(date: Date) {
  return format(date, "yyyy-MM-dd");
}

export async function getWeekendWeather(fridayDateStr: string) {
  const lat = HOMETOWN_COORDINATES.lat;
  const lng = HOMETOWN_COORDINATES.lng;

  const friday = new Date(fridayDateStr);
  const dates = [
    fridayDateStr,
    format(addDays(friday, 1), "yyyy-MM-dd"),
    format(addDays(friday, 2), "yyyy-MM-dd"),
  ];

  try {
    const res = await fetch(
      `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lng}&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto`,
      { next: { revalidate: 3600 } },
    );
    const data = await res.json();

    const weekendForecast = dates.map((date) => {
      const dayIndex = data.daily.time.indexOf(date);
      if (dayIndex === -1) return null;
      return {
        date,
        maxTemp: Math.round(data.daily.temperature_2m_max[dayIndex]),
        minTemp: Math.round(data.daily.temperature_2m_min[dayIndex]),
        code: data.daily.weather_code[dayIndex],
      };
    });

    if (weekendForecast.every((day) => day === null)) return null;

    // For the summary (the card), we use Saturday's weather if available, or the first available day
    const summary =
      weekendForecast[1] || weekendForecast.find((d) => d !== null);

    return {
      summary,
      details: weekendForecast,
    };
  } catch {
    return null;
  }
}

export function getWeatherIcon(code: number) {
  if (code === 0 || code === 1) return "☀️";
  if (code === 2) return "⛅";
  if (code === 3) return "☁️";
  if (code <= 48) return "🌫️";
  if (code <= 67) return "🌧️";
  if (code <= 77) return "❄️";
  if (code <= 82) return "🌦️";
  return "⛈️";
}

export function getWeatherDescription(code: number) {
  if (code === 0) return "Sol radiant";
  if (code === 1 || code === 2) return "Cel clar";
  if (code === 3) return "Nuvolat";
  if (code === 45 || code === 48) return "Hi haurà boira";
  if (code >= 51 && code <= 55) return "Plugim suau";
  if (code >= 61 && code <= 67) return "Pluja";
  if (code >= 71 && code <= 77) return "Neu";
  if (code >= 80 && code <= 82) return "Possibles ruixats";
  if (code >= 95) return "Risc de tempesta";
  return "Temps variable";
}

export function getWhatsAppShareUrl(activity: any) {
  const dayText = getFormattedDayText(activity.weekend_date, activity.day_of_week);
  const timeText = activity.start_time ? ` a les ${activity.start_time}` : "";
  const text = `Ei! Estem organitzant això per KONNECTA:%0A%0A*${activity.title.toUpperCase()}*%0A📅 ${dayText}${timeText}%0A%0AAnima't i apunta't aquí: ${window.location.origin}?date=${activity.weekend_date}`;
  return `https://wa.me/?text=${text}`;
}

export { ca };
