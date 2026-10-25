"use client";
import { Bar, Doughnut } from "react-chartjs-2";
import {
  ArcElement,
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Tooltip,
} from "chart.js";
ChartJS.register(
  ArcElement,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend,
);
const colors = [
  "#16803a",
  "#35a657",
  "#70bd84",
  "#a5d5af",
  "#d2ead7",
  "#2563eb",
];
export function CategoryChart({ values }: { values: Record<string, number> }) {
  const labels = Object.keys(values);
  return (
    <Doughnut
      data={{
        labels,
        datasets: [
          {
            data: Object.values(values),
            backgroundColor: colors,
            borderWidth: 0,
          },
        ],
      }}
      options={{
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: "bottom",
            labels: { usePointStyle: true, padding: 18 },
          },
        },
      }}
    />
  );
}
export function MonthlyChart({ values }: { values: Record<string, number> }) {
  const labels = Object.keys(values);
  return (
    <Bar
      data={{
        labels,
        datasets: [
          {
            label: "Doações",
            data: Object.values(values),
            backgroundColor: "#16803a",
            borderRadius: 8,
            maxBarThickness: 42,
          },
        ],
      }}
      options={{
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          x: { grid: { display: false } },
          y: { beginAtZero: true, ticks: { precision: 0 } },
        },
      }}
    />
  );
}
