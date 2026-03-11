import { useState, useEffect } from 'react'
import styles from './TerminalHeader.module.css'
import { PROJECT_NAME, REPO_NAME, LINKS, AUTHORS } from '../config'

function useGitHubProfile(username) {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const cacheKey = 'gh-profile-' + username

    // Check sessionStorage first — avoids hammering the API on every render
    try {
      const cached = sessionStorage.getItem(cacheKey)
      if (cached) {
        setProfile(JSON.parse(cached))
        setLoading(false)
        return
      }
    } catch (e) {}

    fetch('https://api.github.com/users/' + username)
      .then(r => {
        if (!r.ok) throw new Error('HTTP ' + r.status)
        return r.json()
      })
      .then(d => {
        // Don't cache error responses like rate limit messages
        if (!d.message) {
          try { sessionStorage.setItem(cacheKey, JSON.stringify(d)) } catch (e) {}
        }
        setProfile(d)
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }, [username])

  return { profile, loading }
}

function AuthorTile({ username, active }) {
  const { profile, loading } = useGitHubProfile(username)
  const tileClass = styles.authorTile + (active ? ' ' + styles.authorTileActive : '')

  if (loading) {
    return (
      <div className={tileClass}>
        <div className={styles.authorAvatarSkeleton} />
        <div className={styles.authorInfo}>
          <div className={styles.authorSkeleton} style={{ width: '60px' }} />
          <div className={styles.authorSkeleton} style={{ width: '80px', marginTop: '4px' }} />
          <div className={styles.authorBioPlaceholder} />
        </div>
      </div>
    )
  }

  if (!profile || profile.message) {
    return (
      <a
        href={'https://github.com/' + username}
        target="_blank"
        rel="noopener noreferrer"
        className={tileClass}
      >
        <div className={styles.authorAvatarSkeleton} />
        <div className={styles.authorInfo}>
          <span className={styles.authorName}>{username}</span>
          <span className={styles.authorLogin}>@{username}</span>
          <div className={styles.authorBioPlaceholder} />
        </div>
      </a>
    )
  }

  return (
    <a
      href={profile.html_url}
      target="_blank"
      rel="noopener noreferrer"
      className={tileClass}
    >
      <img
        src={profile.avatar_url}
        alt={profile.login}
        className={styles.authorAvatar}
      />
      <div className={styles.authorInfo}>
        <span className={styles.authorName}>{profile.name || profile.login}</span>
        <span className={styles.authorLogin}>@{profile.login}</span>
        {profile.bio
          ? <span className={styles.authorBio}>{profile.bio}</span>
          : <div className={styles.authorBioPlaceholder} />
        }
      </div>
    </a>
  )
}

function AuthorsTiles() {
  const [active, setActive] = useState(0)

  useEffect(() => {
    if (AUTHORS.length <= 1) return
    const t = setInterval(() => {
      setActive(i => (i + 1) % AUTHORS.length)
    }, 4000)
    return () => clearInterval(t)
  }, [])

  return (
    <div className={styles.authors}>
      <div className={styles.outputLine}>
        <span className={styles.chevron}>›</span> Authors:
      </div>
      <div className={styles.authorRow}>
        {AUTHORS.map((username, i) => (
          <AuthorTile
            key={username}
            username={username}
            active={i === active}
          />
        ))}
      </div>
    </div>
  )
}

export default function TerminalHeader() {
  return (
    <div className={styles.wrap}>
      <div className={styles.bar}>
        <div className={styles.dot + ' ' + styles.red}   />
        <div className={styles.dot + ' ' + styles.amber} />
        <div className={styles.dot + ' ' + styles.green} />
        <span className={styles.title}>javadoc-index — bash</span>
      </div>
      <div className={styles.body}>
        <h1 className={styles.heading}>
          {PROJECT_NAME}
        </h1>

        <div className={styles.promptLine}>
          <span className={styles.prompt}>$</span>
          <span className={styles.cmd}>browse-docs</span>
          <span className={styles.arg}>{REPO_NAME}</span>
          <span className={styles.flag}>--all-versions</span>
        </div>

        <div className={styles.outputLine}>
          <span className={styles.chevron}>›</span> Fetching available documentation builds...
        </div>
        <div className={styles.outputLine}>
          <span className={styles.chevron}>›</span> Found project links:
        </div>

        <div className={styles.links}>
          {LINKS.map(function(l) {
            return (
              <a
                key={l.tag}
                href={l.href}
                target="_blank"
                rel="noopener noreferrer"
                className={styles.link}
              >
                <span className={styles.linkTag} style={{ color: l.color, borderColor: l.color }}>
                  {l.tag}
                </span>
                <span className={styles.linkLabel}>{l.label}</span>
                <span className={styles.linkArrow} style={{ color: l.color }}>↗</span>
              </a>
            )
          })}
        </div>

        <AuthorsTiles />

        <div className={styles.promptLine} style={{ marginTop: '16px' }}>
          <span className={styles.prompt}>$</span>
          <span className={styles.cursor} />
        </div>
      </div>
    </div>
  )
}
