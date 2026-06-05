import whiteImg from "../assets/white.png";
import orangeImg from "../assets/orange.png";
import blueImg from "../assets/blue.png";
import yellowImg from "../assets/yellow.png";
import greenImg from "../assets/green.png";
import brownImg from "../assets/brown.png";
import blackImg from "../assets/black.png";

type BeltBadgeProps = {
  color: string;
  label?: string;
  size?: "sm" | "md" | "lg";
};

const beltImages: Record<string, string> = {
  white: whiteImg,
  orange: orangeImg,
  blue: blueImg,
  yellow: yellowImg,
  green: greenImg,
  brown: brownImg,
  black: blackImg,
};

const sizeClasses = {
  sm: "h-6 w-8",
  md: "h-8 w-10",
  lg: "h-12 w-16",
};

export default function BeltBadge({ color, label, size = "md" }: BeltBadgeProps) {
  const imagePath = beltImages[color.toLowerCase()] ?? blackImg;
  const sizeClass = sizeClasses[size];

  return (
    <div className="flex items-center gap-2">
      <img
        src={imagePath}
        alt={`${color} belt`}
        className={`${sizeClass} object-contain`}
        title={label ?? "Rank belt"}
      />
      {label ? (
        <span className="text-sm font-semibold text-slate-900">{label}</span>
      ) : null}
    </div>
  );
}
