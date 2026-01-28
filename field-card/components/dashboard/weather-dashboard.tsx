"use client";

import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Cloud, CloudFog, CloudLightning, CloudRain, CloudSnow, Sun, Thermometer, Wind, Droplets } from "lucide-react";

const LAT = 52.23;
const LNG = 21.01;

interface WeatherData {
    current: {
        temperature_2m: number;
        wind_speed_10m: number;
        weather_code: number;
    };
    daily: {
        temperature_2m_max: number[];
        temperature_2m_min: number[];
        precipitation_sum: number[];
    };
}

const getWeatherIcon = (code: number) => {
    if (code === 0) return { icon: <Sun className="h-8 w-8 text-yellow-500" />, label: "Bezchmurnie" };
    if (code >= 1 && code <= 3) return { icon: <Cloud className="h-8 w-8 text-gray-400" />, label: "Zachmurzenie umiarkowane" };
    if (code >= 45 && code <= 48) return { icon: <CloudFog className="h-8 w-8 text-slate-400" />, label: "Mgła" };
    if (code >= 51 && code <= 67) return { icon: <CloudRain className="h-8 w-8 text-blue-400" />, label: "Deszcz" };
    if (code >= 71 && code <= 77) return { icon: <CloudSnow className="h-8 w-8 text-white" />, label: "Śnieg" };
    if (code >= 80 && code <= 82) return { icon: <CloudRain className="h-8 w-8 text-blue-600" />, label: "Ulewa" };
    if (code >= 95) return { icon: <CloudLightning className="h-8 w-8 text-purple-500" />, label: "Burza" };
    return { icon: <Sun className="h-8 w-8 text-yellow-500" />, label: "Słonecznie" };
};

const fetchWeather = async (): Promise<WeatherData> => {
    const res = await fetch(
        `https://api.open-meteo.com/v1/forecast?latitude=${LAT}&longitude=${LNG}&current=temperature_2m,weather_code,wind_speed_10m&daily=temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=auto`
    );
    if (!res.ok) throw new Error("Błąd pobierania pogody");
    return res.json();
};

export function WeatherWidget() {
    const { data, isLoading, isError } = useQuery({
        queryKey: ['weather'],
        queryFn: fetchWeather,
        refetchInterval: 1000 * 60 * 30,
    });

    if (isLoading) {
        return (
            <Card>
                <CardHeader><Skeleton className="h-6 w-32" /></CardHeader>
                <CardContent className="space-y-4">
                    <Skeleton className="h-12 w-20" />
                    <Skeleton className="h-4 w-full" />
                </CardContent>
            </Card>
        );
    }

    if (isError || !data) {
        return (
            <Card className="bg-red-50">
                <CardContent className="pt-6 text-red-500 text-sm">Nie udało się załadować pogody.</CardContent>
            </Card>
        );
    }

    const { current, daily } = data;
    const weatherInfo = getWeatherIcon(current.weather_code);
    const tempMax = daily.temperature_2m_max[0];
    const tempMin = daily.temperature_2m_min[0];
    const rainSum = daily.precipitation_sum[0];

    return (
        <Card className="overflow-hidden relative bg-gradient-to-br from-blue-50 to-white border-blue-100 shadow-sm hover:shadow-md transition-shadow">
            <div className="absolute -top-6 -right-6 w-24 h-24 bg-blue-100 rounded-full opacity-50 blur-xl" />

            <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-slate-500 flex items-center gap-2">
                    <MapPinIcon className="h-3 w-3" /> Twoja lokalizacja
                </CardTitle>
            </CardHeader>
            <CardContent>
                <div className="flex justify-between items-start">
                    <div>
                        <div className="text-4xl font-bold text-slate-800 tracking-tighter">
                            {Math.round(current.temperature_2m)}°C
                        </div>
                        <p className="text-sm text-slate-600 font-medium mt-1 flex items-center gap-1">
                            {weatherInfo.label}
                        </p>
                    </div>
                    <div className="p-2 bg-white rounded-full shadow-sm border border-slate-100">
                        {weatherInfo.icon}
                    </div>
                </div>

                <div className="mt-6 grid grid-cols-3 gap-2 border-t pt-4">
                    <div className="flex flex-col items-center justify-center p-2 bg-white/60 rounded-lg">
                        <Wind className="h-4 w-4 text-slate-400 mb-1" />
                        <span className="text-xs font-semibold">{current.wind_speed_10m} km/h</span>
                        <span className="text-[10px] text-slate-400">Wiatr</span>
                    </div>

                    <div className="flex flex-col items-center justify-center p-2 bg-white/60 rounded-lg">
                        <Droplets className="h-4 w-4 text-blue-400 mb-1" />
                        <span className="text-xs font-semibold">{rainSum} mm</span>
                        <span className="text-[10px] text-slate-400">Opady dzisiaj</span>
                    </div>

                    <div className="flex flex-col items-center justify-center p-2 bg-white/60 rounded-lg">
                        <Thermometer className="h-4 w-4 text-red-400 mb-1" />
                        <span className="text-xs font-semibold">{Math.round(tempMax)}° / {Math.round(tempMin)}°</span>
                        <span className="text-[10px] text-slate-400">Max / Min</span>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
}

function MapPinIcon(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg
            {...props}
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
        >
            <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z" />
            <circle cx="12" cy="10" r="3" />
        </svg>
    )
}