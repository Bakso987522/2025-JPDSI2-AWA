"use client"
import {useEffect, useState} from "react";
import {Button} from "@/components/ui/button";

export default function Navbar(){
    const [isScrolled, setIsScrolled] = useState(false);
    useEffect(() => {
        const handleScroll = () => {
            setIsScrolled((window.scrollY > 10));
        };
        window.addEventListener("scroll", handleScroll);
        return () => {
            window.removeEventListener("scroll", handleScroll);
        };
    }, [])
    return (
        <header className={"sticky top-0 z-50 w-full transition-all duration-300 h-14" + (isScrolled ? "bg-background/40 backdrop-blur-md border-background/80" : "")}>
            <section className={"h-full mx-auto w-full max-w-full md:max-w-screen-xl px-4 md:px-12 lg:px-20 flex items-center justify-between"}>
                <div>
                    <p className={"text-xl font-bold"}>FieldCard</p>
                </div>
                <nav>
                    <ul className={"flex gap-5"}>
                        <li>
                            <a href="#">Features</a>
                        </li>
                        <li>
                            <a href="#">Pricing</a>
                        </li>
                        <li>
                            <a href="#">Resources</a>
                        </li>
                        <li>
                            <a href="#">About</a>
                        </li>
                    </ul>
                </nav>
                <div>
                    <Button>Get Started</Button>
                </div>
            </section>
        </header>
    )
}