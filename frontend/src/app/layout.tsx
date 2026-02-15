import type { Metadata } from "next";
import localFont from 'next/font/local';
import './globals.css';

//load the local font
const departureMono = localFont({
  src: "../fonts/DepartureMono-Regular.woff2",
  variable: "--font-departure",
  display: "swap"
});

export const metadata: Metadata = {
  title: "STREAMFORGE",
  description: "a media store platform"
};

export default function RootLayout({
  children
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className={`${departureMono.variable} font-mono bg-[#E6E6E6] text-[#1A1A1A]`}>
        {children}
      </body>
    </html>
  )
}
