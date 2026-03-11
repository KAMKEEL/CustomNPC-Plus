import styles from './Card.module.css'
import { REPO_NAME } from '../config'

const BASE_PATH = `/${REPO_NAME}`

function formatDate(iso) {
  if (!iso) return null
  try {
    return new Intl.DateTimeFormat('en-GB', {
      day: '2-digit', month: 'short', year: 'numeric'
    }).format(new Date(iso))
  } catch (e) { return null }
}

export function ReleaseCard({ release, index }) {
  const isLatest = release.isLatest
  const date = formatDate(release.date)
  const cardClass = isLatest
    ? styles.card + ' ' + styles.latest
    : styles.card + ' ' + styles.release

  return (
    <a
      href={BASE_PATH + '/' + release.path + '/'}
      className={cardClass}
      style={{ animationDelay: (index * 50) + 'ms' }}
    >
      <div className={styles.header}>
        <div className={styles.badges}>
          {isLatest && (
            <span className={styles.badge + ' ' + styles.badgeLatest}>● latest</span>
          )}
          <span className={styles.badge + ' ' + styles.badgeRelease}>release</span>
        </div>
        <span className={styles.arrow}>→</span>
      </div>

      <div className={styles.version}>
        v{release.version}
      </div>
        <div className={styles.meta}>
            {date && (
              <div className={styles.metaRow}>
                <span className={styles.metaKey}>date</span>
                <span className={styles.metaVal}>{date}</span>
              </div>
            )}
            <div className={styles.metaRow}>
              <span className={styles.metaKey}>path</span>
              <span className={styles.metaVal}>{release.path}/</span>
            </div>
      </div>
    </a>
  )
}

export function ExperimentalCard({ branch, index }) {
  const date = formatDate(branch.date)

  return (
    <a
      href={BASE_PATH + '/' + branch.path + '/'}
      className={styles.card + ' ' + styles.experimental}
      style={{ animationDelay: (index * 50) + 'ms' }}
    >
      <div className={styles.header}>
        <span className={styles.badge + ' ' + styles.badgeExperimental}>
          experimental
        </span>
        <span className={styles.arrow}>→</span>
      </div>

      <div className={styles.version + ' ' + styles.versionBranch}>
        {branch.branch}
      </div>

      <div className={styles.meta}>
        {date && (
          <div className={styles.metaRow}>
            <span className={styles.metaKey}>date</span>
            <span className={styles.metaVal}>{date}</span>
          </div>
        )}
        {branch.hash && (
          <div className={styles.metaRow}>
            <span className={styles.metaKey}>sha</span>
            <span className={styles.metaVal + ' ' + styles.hash}>{branch.hash}</span>
          </div>
        )}
        <div className={styles.metaRow}>
          <span className={styles.metaKey}>path</span>
          <span className={styles.metaVal}>{branch.path}/</span>
        </div>
      </div>
    </a>
  )
}
