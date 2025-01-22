import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import VideoUpload from './components/VideoUpload'

function App() {

  return (
    <>
      <div className='flex flex-col items-center space-y-9 justify-center py-9'>
        <h1 className='text-4xl font-extrabold text-gray-700 dark:text-gray-100'>Welcome to Video Streaming Application ! </h1>
        <video
          src={`http://localhost:8080/api/v1/stream/range/785919db-c4cd-44b4-a998-41bf5a092a18`}
          controls
        >          
        </video>
        <VideoUpload/>
      </div>
    </>
  )
}

export default App
