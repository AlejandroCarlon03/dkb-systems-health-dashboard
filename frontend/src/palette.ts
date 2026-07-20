// Categorical palette for chart segments — reasonably distinct in light and dark themes.
export const PALETTE = [
  '#2f6fed', // blue
  '#1f9d55', // green
  '#d98a00', // amber
  '#8b5cf6', // violet
  '#e05299', // pink
  '#0ea5e9', // sky
  '#64748b', // slate
]

export function colorFor(index: number): string {
  return PALETTE[index % PALETTE.length]
}
