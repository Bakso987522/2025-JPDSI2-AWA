"use client";

import { useState, useEffect } from "react";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Loader2, MapPin } from "lucide-react";
import { client } from "@/lib/api-client";

interface Region {
    id: string;
    name: string;
    type: string;
}

interface ParcelSelectorProps {
    onParcelSelected: (fullId: string) => void;
}

export function TerytParcelSelector({ onParcelSelected }: ParcelSelectorProps) {
    const [voivodeships, setVoivodeships] = useState<Region[]>([]);
    const [counties, setCounties] = useState<Region[]>([]);
    const [communes, setCommunes] = useState<Region[]>([]);
    const [precincts, setPrecincts] = useState<Region[]>([]);

    const [selectedVoivodeship, setSelectedVoivodeship] = useState("");
    const [selectedCounty, setSelectedCounty] = useState("");
    const [selectedCommune, setSelectedCommune] = useState("");
    const [selectedPrecinct, setSelectedPrecinct] = useState("");

    const [manualNumber, setManualNumber] = useState("");

    const [loading, setLoading] = useState(false);

    const fetchRegions = async (parentId: string = "") => {
        setLoading(true);
        try {
            const url = `/api/location/regions${parentId ? `?parentId=${parentId}` : ''}`;
            const data = await client<Region[]>(url);
            return Array.isArray(data) ? data : [];
        } catch (e) {
            console.error("Błąd pobierania regionów:", e);
            return [];
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchRegions().then(setVoivodeships); }, []);

    useEffect(() => {
        if (!selectedVoivodeship) { setCounties([]); return; }
        setCounties([]); setCommunes([]); setPrecincts([]);
        setSelectedCounty(""); setSelectedCommune(""); setSelectedPrecinct("");
        fetchRegions(selectedVoivodeship).then(setCounties);
    }, [selectedVoivodeship]);

    useEffect(() => {
        if (!selectedCounty) { setCommunes([]); return; }
        setCommunes([]); setPrecincts([]);
        setSelectedCommune(""); setSelectedPrecinct("");
        fetchRegions(selectedCounty).then(setCommunes);
    }, [selectedCounty]);

    useEffect(() => {
        if (!selectedCommune) { setPrecincts([]); return; }
        setPrecincts([]); setSelectedPrecinct("");
        fetchRegions(selectedCommune).then(setPrecincts);
    }, [selectedCommune]);


    const handleAddManual = () => {
        if (!selectedPrecinct || !manualNumber) return;


        const fullId = `${selectedPrecinct}.${manualNumber.trim()}`;

        onParcelSelected(fullId);
        setManualNumber("");
    };

    return (
        <div className="grid gap-4 p-4 border rounded-md bg-slate-50">
            <div className="flex items-center justify-between">
                <h3 className="font-semibold text-sm text-slate-700">Lokalizacja (GUGiK)</h3>
                {loading && <Loader2 className="h-4 w-4 animate-spin text-blue-600" />}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div className="space-y-1">
                    <Label className="text-xs text-slate-500">Województwo</Label>
                    <Select value={selectedVoivodeship} onValueChange={setSelectedVoivodeship}>
                        <SelectTrigger className="bg-white"><SelectValue placeholder="Wybierz..." /></SelectTrigger>
                        <SelectContent>
                            {voivodeships?.map(v => (
                                <SelectItem key={v.id} value={v.id}>{v.name}</SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>

                <div className="space-y-1">
                    <Label className="text-xs text-slate-500">Powiat</Label>
                    <Select value={selectedCounty} onValueChange={setSelectedCounty} disabled={!selectedVoivodeship}>
                        <SelectTrigger className="bg-white"><SelectValue placeholder="Wybierz..." /></SelectTrigger>
                        <SelectContent>
                            {counties?.map(c => (
                                <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>

                <div className="space-y-1">
                    <Label className="text-xs text-slate-500">Gmina</Label>
                    <Select value={selectedCommune} onValueChange={setSelectedCommune} disabled={!selectedCounty}>
                        <SelectTrigger className="bg-white"><SelectValue placeholder="Wybierz..." /></SelectTrigger>
                        <SelectContent>
                            {communes?.map(c => (
                                <SelectItem key={c.id} value={c.id}>{c.name} <span className="text-gray-400 text-xs">({c.type})</span></SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>

                <div className="space-y-1">
                    <Label className="text-xs text-slate-500">Obręb ewidencyjny</Label>
                    <Select value={selectedPrecinct} onValueChange={setSelectedPrecinct} disabled={!selectedCommune}>
                        <SelectTrigger className="bg-white"><SelectValue placeholder="Wybierz..." /></SelectTrigger>
                        <SelectContent>
                            {precincts?.map(p => (
                                <SelectItem key={p.id} value={p.id}>{p.name}</SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>
            </div>


                <div className="pt-3 border-t mt-1">
                    <Label className="text-xs  text-slate-500 mb-1.5 block">
                        Numer działki
                    </Label>
                    <div className="flex gap-2">
                        <Input
                            placeholder="np. 123/4"
                            value={manualNumber}
                            onChange={e => setManualNumber(e.target.value)}
                            onKeyDown={e => e.key === "Enter" && (e.preventDefault(), handleAddManual())}
                            className="bg-white"
                            autoFocus
                            disabled={!selectedPrecinct}
                        />
                        <Button
                            onClick={handleAddManual}
                            disabled={!manualNumber}
                            type="button"
                        >
                            Dodaj
                        </Button>
                    </div>
                    <p className="text-[10px] text-muted-foreground mt-1">
                        Wybrany obręb: <span className="font-mono">{selectedPrecinct}</span>. Wpisz numer i zatwierdź.
                    </p>
                </div>

        </div>
    );
}