import styles from './States.module.css'

export function LoadingState() {
  return (
    <div className={styles.box}>
      <div className={styles.spinner} />
      <p className={styles.label}>Fetching documentation index...</p>
    </div>
  )
}

export function ErrorState({ message }) {
  return (
    <div className={styles.box}>
      <div className={styles.errorIcon}>⚠</div>
      <p className={styles.errorMsg}>{message}</p>
      <p className={styles.errorSub}>
        Check that the gh-pages branch exists and is publicly accessible.
      </p>
    </div>
  )
}

export function EmptyState({ label }) {
  return (
    <div className={`${styles.box} ${styles.empty}`}>
      <p className={styles.label}>No {label} found.</p>
    </div>
  )
}
