/** List of per-source error messages, shown only when something failed. */
export function SourceErrors({ errors }: { errors: string[] }) {
  if (errors.length === 0) return null
  return (
    <>
      <div className="section-title">Source errors</div>
      <div className="errors">
        <ul>
          {errors.map((e, i) => (
            <li key={i}>
              <code>{e}</code>
            </li>
          ))}
        </ul>
      </div>
    </>
  )
}
