import styles from './App.module.css'
import { useDocs } from './useDocs'
import TerminalHeader from './components/TerminalHeader'
import Section from './components/Section'
import { ReleaseCard, ExperimentalCard } from './components/Card'
import { LoadingState, ErrorState, EmptyState } from './components/States'
import Footer from './components/Footer'

export default function App() {
  const { data, loading, error } = useDocs()

  const releases     = data?.releases     || []
  const experimental = data?.experimental || []

  return (
    <div className={styles.root}>
      <TerminalHeader />

      {/* ── Releases ── */}
      <Section
        label="Release"
        variant="releases"
        count={loading ? undefined : releases.length}
      >
        {loading ? (
          <LoadingState />
        ) : error ? (
          <ErrorState message={error} />
        ) : releases.length === 0 ? (
          <EmptyState label="releases" />
        ) : (
          <div className={styles.grid}>
            {releases.map((r, i) => (
              <ReleaseCard key={r.id} release={r} index={i} />
            ))}
          </div>
        )}
      </Section>

      {/* ── Experimental ── */}
      <Section
        label="Experimental"
        variant="experimental"
        count={loading ? undefined : experimental.length}
      >
        {loading ? (
          <LoadingState />
        ) : error ? null : experimental.length === 0 ? (
          <EmptyState label="experimental branches" />
        ) : (
          <div className={styles.grid}>
            {experimental.map((b, i) => (
              <ExperimentalCard key={b.id} branch={b} index={i} />
            ))}
          </div>
        )}
      </Section>

      {!loading && !error && (
        <Footer source={data?.source} />
      )}
    </div>
  )
}
