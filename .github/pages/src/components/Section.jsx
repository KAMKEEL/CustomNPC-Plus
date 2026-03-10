import styles from './Section.module.css'

export default function Section({ label, count, variant, children }) {
  return (
    <section className={styles.section}>
      <div className={styles.header}>
        <span className={`${styles.tag} ${styles[variant]}`}>{label}</span>
        <div className={styles.line} />
        {count !== undefined && (
          <span className={styles.count}>
            {count} {label.toLowerCase()}{count !== 1 ? 's' : ''}
          </span>
        )}
      </div>
      {children}
    </section>
  )
}
