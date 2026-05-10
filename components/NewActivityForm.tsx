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
      className="bg-white dark:bg-zinc-900 border border-zinc-100 dark:border-zinc-800 p-5 rounded-[2rem] shadow-xl space-y-5 relative animate-in zoom-in-95 duration-200 w-full max-w-full overflow-hidden"
    >
      <button
        type="button"
        onClick={() => {
          setIsOpen(false);
          setError(null);
        }}
        className="absolute top-4 right-4 text-zinc-400 hover:text-zinc-600 p-1"
      >
        <X size={20} />
      </button>

      <h3 className="text-lg font-bold tracking-tight text-zinc-950 dark:text-white uppercase px-1">
        Nou Pla
      </h3>

      {error && (
        <p className="text-xs font-bold text-red-500 bg-red-50 dark:bg-red-900/20 p-3 rounded-xl mx-1">
          {error}
        </p>
      )}

      <input type="hidden" name="weekend_date" value={weekendDate} />
      <input type="hidden" name="day_of_week" value={selectedDay} />
      <input type="hidden" name="groupId" value={groupId} />
      <input type="hidden" name="start_date" value={startDate} />
      <input type="hidden" name="end_date" value={isMultiDay ? endDate : ""} />

      <div className="space-y-5">
        {/* Selector de Dia Ràpid */}
        <div className="flex gap-2 p-1 bg-zinc-100 dark:bg-zinc-800 rounded-2xl mx-1">
          {daysData.map((day) => (
            <button
              key={day.id}
              type="button"
              onClick={() => handleDaySelect(day.id, day.date)}
              className={`flex-1 py-2 flex flex-col items-center rounded-xl transition-all ${
                selectedDay === day.id && !isFlexible
                  ? "bg-white dark:bg-zinc-700 text-zinc-950 dark:text-white shadow-sm scale-[1.02]"
                  : "text-zinc-400"
              }`}
            >
              <span className="text-[10px] font-black uppercase tracking-wider leading-none">
                {day.label}
              </span>
              <span className="text-sm font-bold mt-0.5">
                {format(day.date, "d")}
              </span>
            </button>
          ))}
          <button
            type="button"
            onClick={() => setIsFlexible(true)}
            className={`flex-1 py-2 flex flex-col items-center justify-center rounded-xl transition-all ${
              isFlexible
                ? "bg-white dark:bg-zinc-700 text-zinc-950 dark:text-white shadow-sm scale-[1.02]"
                : "text-zinc-400"
            }`}
          >
            <span className="text-[10px] font-black uppercase tracking-wider leading-none">Altres</span>
            <span className="text-sm font-bold mt-0.5">...</span>
          </button>
        </div>

        {/* Grup d'Inici */}
        <div className="space-y-3 px-1">
          <div className="flex items-center gap-2 text-[10px] font-black text-zinc-400 uppercase tracking-widest">
            <div className="w-1.5 h-1.5 rounded-full bg-blue-500" />
            Inici del pla
          </div>
          
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {isFlexible && (
              <div className="relative">
                <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" size={14} />
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="w-full pl-9 pr-3 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-blue-500/20 font-bold text-zinc-950 dark:text-white text-sm"
                />
              </div>
            )}
            
            <div className="flex gap-2">
              <div className="relative flex-1">
                <Clock className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" size={14} />
                <select
                  name="start_hour"
                  defaultValue="19"
                  className="w-full pl-9 pr-2 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-blue-500/20 font-bold text-zinc-950 dark:text-white appearance-none text-center text-sm"
                >
                  {Array.from({ length: 24 }).map((_, i) => (
                    <option key={i} value={i.toString().padStart(2, "0")}>
                      {i.toString().padStart(2, "0")}h
                    </option>
                  ))}
                </select>
              </div>
              <div className="flex items-center font-bold text-zinc-300">:</div>
              <select
                name="start_minute"
                defaultValue="00"
                className="flex-1 px-2 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-blue-500/20 font-bold text-zinc-950 dark:text-white appearance-none text-center text-sm"
              >
                {["00", "15", "30", "45"].map((m) => (
                  <option key={m} value={m}>
                    {m}m
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="flex items-center gap-2 px-1">
            <input 
              type="checkbox" 
              id="multi-day" 
              checked={isMultiDay} 
              onChange={(e) => setIsMultiDay(e.target.checked)}
              className="w-4 h-4 rounded border-zinc-300 text-blue-500 focus:ring-blue-500"
            />
            <label htmlFor="multi-day" className="text-[10px] font-black text-zinc-500 uppercase tracking-widest cursor-pointer">Afegir data/hora de finalització</label>
          </div>
        </div>

        {/* Grup de Finalització (Condicional) */}
        {isMultiDay && (
          <div className="space-y-3 px-1 animate-in fade-in slide-in-from-top-2 duration-200">
            <div className="flex items-center gap-2 text-[10px] font-black text-zinc-400 uppercase tracking-widest">
              <div className="w-1.5 h-1.5 rounded-full bg-red-400" />
              Final del pla
            </div>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="relative">
                <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" size={14} />
                <input
                  type="date"
                  value={endDate}
                  min={startDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="w-full pl-9 pr-3 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-blue-500/20 font-bold text-zinc-950 dark:text-white text-sm"
                />
              </div>
              
              <div className="flex gap-2">
                <div className="relative flex-1">
                  <Clock className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" size={14} />
                  <select
                    name="end_hour"
                    defaultValue="22"
                    className="w-full pl-9 pr-2 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-blue-500/20 font-bold text-zinc-950 dark:text-white appearance-none text-center text-sm"
                  >
                    {Array.from({ length: 24 }).map((_, i) => (
                      <option key={i} value={i.toString().padStart(2, "0")}>
                        {i.toString().padStart(2, "0")}h
                      </option>
                    ))}
                  </select>
                </div>
                <div className="flex items-center font-bold text-zinc-300">:</div>
                <select
                  name="end_minute"
                  defaultValue="00"
                  className="flex-1 px-2 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-blue-500/20 font-bold text-zinc-950 dark:text-white appearance-none text-center text-sm"
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
        <div className="space-y-4 pt-2">
          <div className="space-y-1 px-1">
            <input
              name="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Títol del pla"
              required
              maxLength={50}
              className="w-full px-4 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-zinc-900 dark:focus:ring-zinc-100 font-bold text-zinc-950 dark:text-white"
            />
            <div className="flex justify-end px-1">
              <span className={`text-[8px] font-black ${title.length >= 45 ? 'text-red-500' : 'text-zinc-400'}`}>
                {title.length}/50
              </span>
            </div>
          </div>

          <div className="space-y-1 px-1">
            <textarea
              name="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Detalls (opcional)"
              rows={2}
              maxLength={200}
              className="w-full px-4 py-3 rounded-xl bg-zinc-50 dark:bg-zinc-800 border border-zinc-100 dark:border-zinc-700 outline-none focus:ring-2 focus:ring-zinc-900 dark:focus:ring-zinc-100 font-medium text-zinc-950 dark:text-white"
            />
            <div className="flex justify-end px-1">
              <span className={`text-[8px] font-black ${description.length >= 180 ? 'text-red-500' : 'text-zinc-400'}`}>
                {description.length}/200
              </span>
            </div>
          </div>
        </div>
      </div>

      <button
        type="submit"
        disabled={isPending}
        className="w-full py-4 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black transition-transform active:scale-95 shadow-lg disabled:opacity-50"
      >
        {isPending ? "CREANT..." : "AFEGIR PLA"}
      </button>
    </form>
  );
}
