import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import { PROJECT_NAME } from './config'
import './index.css'

document.title = `${PROJECT_NAME} — Javadoc Index`

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
