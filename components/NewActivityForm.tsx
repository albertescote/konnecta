"use client";

import { useState } from "react";
import { createActivity } from "@/app/actions/activities";
import { Plus, X, Clock, Calendar } from "lucide-react";
import { parseISO, addDays, format } from "date-fns";

export default function NewActivityForm({
  weekendDate,
  groupId,
}: {
  weekendDate: string;
  groupId: string;
}) {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedDay, setSelectedDay] = useState("dissabte");
  const [isFlexible, setIsFlexible] = useState(false);
  const anchorDate = parseISO(weekendDate);
  const initialStartDate = format(addDays(anchorDate, 1), "yyyy-MM-dd");
  const [startDate, setStartDate] = useState(initialStartDate);
  const [endDate, setEndDate] = useState<string>("");
  const [isMultiDay, setIsMultiDay] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isPending, setIsPending] = useState(false);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");

  const daysData = [
    { id: "divendres", label: "Div", date: anchorDate },
    { id: "dissabte", label: "Dis", date: addDays(anchorDate, 1) },
    { id: "diumenge", label: "Diu", date: addDays(anchorDate, 2) },
  ];

  const handleDaySelect = (dayId: string, date: Date) => {
    setSelectedDay(dayId);
    setStartDate(format(date, "yyyy-MM-dd"));
    setIsFlexible(false);
    setIsMultiDay(false);
  };

  if (!isOpen) {
    return (
      <button
        onClick={() => setIsOpen(true)}
        className="w-full py-4 border-2 border-dashed border-zinc-200 dark:border-zinc-800 rounded-3xl flex items-center justify-center gap-2 text-zinc-400 font-bold hover:bg-zinc-50 dark:hover:bg-zinc-900/50 transition-colors"
      >
        <Plus size={20} />
        Proposa un Pla
      </button>
    );
  }

  return (
    <form
      action={async (formData) => {
        setIsPending(true);
        setError(null);

        const startHour = formData.get("start_hour");
        const startMinute = formData.get("start_minute");
        formData.set("start_time", `${startHour}:${startMinute}`);

        if (isMultiDay || isFlexible) {
          const endHour = formData.get("end_hour");
          const endMinute = formData.get("end_minute");
          formData.set("end_time", `${endHour}:${endMinute}`);
        }

        const res = await createActivity(formData);
        setIsPending(false);
        if (res.success) {
          setIsOpen(false);
          setTitle("");
          setDescription("");
          setIsMultiDay(false);
        } else {
          setError(res.error || "Alguna cosa ha anat malament");
        }
      }}
      className="bg-white dark:bg-zinc-900 border border-zinc-100 dark:border-zinc-800 p-6 rounded-[2.5rem] shadow-xl space-y-6 relative animate-in zoom-in-95 duration-200 w-full max-w-full overflow-hidden box-border flex flex-col"
    >
      <button
        type="button"
        onClick={() => {
          setIsOpen(false);
          setError(null);
        }}
        className="absolute top-5 right-5 text-zinc-400 hover:text-zinc-600 p-1"
      >
        <X size={22} />
      </button>

      <h3 className="text-xl font-black tracking-tight text-zinc-950 dark:text-white uppercase">
        Nou Pla
      </h3>

      {error && (
        <p className="text-xs font-bold text-red-500 bg-red-50 dark:bg-red-900/20 p-4 rounded-2xl">
          {error}
        </p>
      )}

      <input type="hidden" name="weekend_date" value={weekendDate} />
      <input type="hidden" name="day_of_week" value={selectedDay} />
      <input type="hidden" name="groupId" value={groupId} />
      <input type="hidden" name="start_date" value={startDate} />
      <input type="hidden" name="end_date" value={isMultiDay ? endDate : ""} />

      <div className="space-y-6 w-full flex-1">
        {/* Selector de Dia Ràpid */}
        <div className="flex gap-2 p-1 bg-zinc-100 dark:bg-zinc-800 rounded-2xl w-full">
          {daysData.map((day) => (
            <button
              key={day.id}
              type="button"
              onClick={() => handleDaySelect(day.id, day.date)}
              className={`flex-1 py-2.5 flex flex-col items-center rounded-xl transition-all ${
                selectedDay === day.id && !isFlexible
                  ? "bg-white dark:bg-zinc-700 text-zinc-950 dark:text-white shadow-sm scale-[1.02]"
                  : "text-zinc-400"
              }`}
            >
              <span className="text-[10px] font-black uppercase tracking-wider leading-none">
                {day.label}
              </span>
              <span className="text-sm font-bold mt-1">
                {format(day.date, "d")}
              </span>
            </button>
          ))}
          <button
            type="button"
            onClick={() => setIsFlexible(true)}
            className={`flex-1 py-2.5 flex flex-col items-center justify-center rounded-xl transition-all ${
              isFlexible
                ? "bg-white dark:bg-zinc-700 text-zinc-950 dark:text-white shadow-sm scale-[1.02]"
                : "text-zinc-400"
            }`}
          >
            <span className="text-[10px] font-black uppercase tracking-wider leading-none">Altres</span>
            <span className="text-sm font-bold mt-1">...</span>
          </button>
        </div>

        {/* Grup d'Inici */}
        <div className="space-y-4 w-full">
          <div className="flex items-center gap-2 text-[10px] font-black text-zinc-400 uppercase tracking-widest px-1">
            <div className="w-1.5 h-1.5 rounded-full bg-blue-500" />
            Inici del pla
          </div>
          
          <div className="flex flex-col gap-4 w-full">
            {isFlexible && (
              <div className="w-full relative flex items-center">
                <Calendar className="absolute left-4 z-10 text-zinc-400 pointer-events-none" size={16} />
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  style={{ width: '100%', minWidth: '0', flex: '1 1 0%' }}
                  className="appearance-none m-0 pl-11 pr-4 py-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-blue-500/20 font-bold text-zinc-950 dark:text-white text-base box-border"
                />
              </div>
            )}
            
            <div className="flex gap-2 w-full">
              <div className="relative flex-1 min-w-0">
                <Clock className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-400 pointer-events-none" size={16} />
                <select
                  name="start_hour"
                  defaultValue="19"
                  className="w-full appearance-none m-0 pl-11 pr-2 py-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-blue-500/20 font-bold text-zinc-950 dark:text-white text-center text-base box-border"
                >
                  {Array.from({ length: 24 }).map((_, i) => (
                    <option key={i} value={i.toString().padStart(2, "0")}>
                      {i.toString().padStart(2, "0")}h
                    </option>
                  ))}
                </select>
              </div>
              <div className="flex items-center font-bold text-zinc-300 text-lg px-1">:</div>
              <select
                name="start_minute"
                defaultValue="00"
                className="flex-1 min-w-0 appearance-none m-0 px-2 py-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-blue-500/20 font-bold text-zinc-950 dark:text-white text-center text-base box-border"
              >
                {["00", "15", "30", "45"].map((m) => (
                  <option key={m} value={m}>
                    {m}m
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="flex items-center gap-3 px-1 py-1">
            <input 
              type="checkbox" 
              id="multi-day" 
              checked={isMultiDay} 
              onChange={(e) => setIsMultiDay(e.target.checked)}
              className="w-5 h-5 rounded-lg border-zinc-300 text-blue-500 focus:ring-blue-500 transition-all"
            />
            <label htmlFor="multi-day" className="text-[11px] font-black text-zinc-500 uppercase tracking-widest cursor-pointer select-none">Afegir finalització</label>
          </div>
        </div>

        {/* Grup de Finalització (Condicional) */}
        {isMultiDay && (
          <div className="space-y-4 animate-in fade-in slide-in-from-top-2 duration-200 pt-2 border-t border-zinc-50 dark:border-zinc-800/50 w-full">
            <div className="flex items-center gap-2 text-[10px] font-black text-zinc-400 uppercase tracking-widest px-1">
              <div className="w-1.5 h-1.5 rounded-full bg-red-400" />
              Final del pla
            </div>
            
            <div className="flex flex-col gap-4 w-full">
              <div className="w-full relative flex items-center">
                <Calendar className="absolute left-4 z-10 text-zinc-400 pointer-events-none" size={16} />
                <input
                  type="date"
                  value={endDate}
                  min={startDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  style={{ width: '100%', minWidth: '0', flex: '1 1 0%' }}
                  className="appearance-none m-0 pl-11 pr-4 py-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-blue-500/20 font-bold text-zinc-950 dark:text-white text-base box-border"
                />
              </div>
              
              <div className="flex gap-2 w-full">
                <div className="relative flex-1 min-w-0">
                  <Clock className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-400 pointer-events-none" size={16} />
                  <select
                    name="end_hour"
                    defaultValue="22"
                    className="w-full appearance-none m-0 pl-11 pr-2 py-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-blue-500/20 font-bold text-zinc-950 dark:text-white text-center text-base box-border"
                  >
                    {Array.from({ length: 24 }).map((_, i) => (
                      <option key={i} value={i.toString().padStart(2, "0")}>
                        {i.toString().padStart(2, "0")}h
                      </option>
                    ))}
                  </select>
                </div>
                <div className="flex items-center font-bold text-zinc-300 text-lg px-1">:</div>
                <select
                  name="end_minute"
                  defaultValue="00"
                  className="flex-1 min-w-0 appearance-none m-0 px-2 py-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-blue-500/20 font-bold text-zinc-950 dark:text-white text-center text-base box-border"
                >
                  {["00", "15", "30", "45"].map((m) => (
                    <option key={m} value={m}>
                      {m}m
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>
        )}

        {/* Títol i Detalls */}
        <div className="space-y-5 pt-4 border-t border-zinc-50 dark:border-zinc-800/50 w-full">
          <div className="space-y-2 w-full">
            <input
              name="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Títol del pla"
              required
              maxLength={50}
              style={{ width: '100%', minWidth: '0' }}
              className="appearance-none m-0 px-5 py-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-zinc-900 dark:focus:ring-zinc-100 font-bold text-zinc-950 dark:text-white text-base box-border"
            />
            <div className="flex justify-end pr-2">
              <span className={`text-[9px] font-black ${title.length >= 45 ? 'text-red-500' : 'text-zinc-400'}`}>
                {title.length}/50
              </span>
            </div>
          </div>

          <div className="space-y-2 w-full">
            <textarea
              name="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Detalls (opcional)"
              rows={3}
              maxLength={200}
              style={{ width: '100%', minWidth: '0' }}
              className="appearance-none m-0 px-5 py-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-zinc-900 dark:focus:ring-zinc-100 font-medium text-zinc-950 dark:text-white text-base box-border resize-none"
            />
            <div className="flex justify-end pr-2">
              <span className={`text-[9px] font-black ${description.length >= 180 ? 'text-red-500' : 'text-zinc-400'}`}>
                {description.length}/200
              </span>
            </div>
          </div>
        </div>
      </div>

      <button
        type="submit"
        disabled={isPending}
        className="w-full py-5 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-[1.5rem] font-black text-sm tracking-widest transition-all active:scale-95 shadow-xl disabled:opacity-50 mt-2"
      >
        {isPending ? "CREANT..." : "AFEGIR PLA"}
      </button>
    </form>
  );
}
