import React, { useState } from "react";
import videoUploadImage from "../assets/video-camera.png";
import { Alert, Button, Card, Label, Progress, Textarea, TextInput } from "flowbite-react";
import axios from "axios";

function VideoUpload() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [meta, setMeta] = useState({
    title: "",
    description: "",
  });
  const [progress, setProgress] = useState(0);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState("");

  function handleFileChange(event) {
    console.log(event);
    setSelectedFile(event.target.files[0]);
  }

  function formFieldChangeEvent(event) {
    setMeta({
      ...meta,
      [event.target.name]: event.target.value,
    });
    console.log(meta);
  }

  function handleForm(formEvent) {
    formEvent.preventDefault();
    if (!selectedFile) {
      alert('Select File !!');
      return;
    }
    // submit the form to server
    saveVideoToServer(selectedFile, meta);
  }

  async function saveVideoToServer(video, videoMetaData) {
    setUploading(true);

    let formData = new FormData();
    formData.append("title", videoMetaData.title);
    formData.append("description", videoMetaData.description);
    formData.append("file", selectedFile);

    try{
      let response = await axios.post(
        `http://localhost:8080/api/v1`,
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
          onUploadProgress: (ProgressEvent) => {
            console.log(ProgressEvent);

            const progress = Math.round((ProgressEvent.loaded*100)/ProgressEvent.total);

            setProgress(progress);

          }
        }
      )
      console.log(response);
      setMessage("File Uploaded !!");
      setUploading(false);
    }catch(error) {
      setUploading(false);
      setMessage("Error in uploading file");
      console.log(error);
    }
  }

  return (
    <div className="text-white p-6 bg-gray-800">
      <Card className="bg-gray-900 rounded-lg shadow-lg">
        <h1 className="text-2xl font-bold text-center mb-6">Upload Video!</h1>
        <form
          className="space-y-6 outline-none border-none"
          onSubmit={handleForm}
        >
          <div className="space-y-4">
            <div>
              <Label value="Video Title" className="text-lg font-medium" />
              <TextInput
                id="video-title"
                type="text"
                placeholder="Enter video title"
                name="title"
                className="mt-2 w-full p-2 border border-gray-300 rounded-md"
                onChange={formFieldChangeEvent}
              />
            </div>
            <div>
              <Label
                htmlFor="comment"
                value="Video Description"
                className="text-lg font-medium"
              />
              <Textarea
                id="comment"
                name="description"
                rows={4}
                className="mt-2 w-full p-2 border border-gray-300 rounded-md"
                placeholder="Enter video description"
                onChange={formFieldChangeEvent}
              />
            </div>
          </div>
          <div className="flex items-center space-x-6">
            <div className="shrink-0">
              <img
                className="h-16 w-16 object-cover rounded-full border border-gray-300"
                src={videoUploadImage}
                alt="Current profile photo"
              />
            </div>
            <label className="block">
              <span className="text-sm font-medium">Choose profile photo</span>
              <input
                onChange={handleFileChange}
                type="file"
                name="file"
                className="mt-2 block w-full text-sm text-gray-500
              file:mr-4 file:py-2 file:px-4
              file:rounded-full file:border-0
              file:text-sm file:font-semibold
              file:bg-violet-50 file:text-violet-700
              hover:file:bg-violet-100"
              />
            </label>
          </div>
          <div>
            {uploading && (
              <Progress
              progress={progress}
              textLabel="Uploading.."
              size={"lg"}
              labelProgress
              labelText
            />
            )}
          </div>
          {message && (
            <Alert color={"success"} onDismiss={()=>{setMessage('')}}>              
              <span className="font-medium">Success Alert !!</span>
              {message}
            </Alert>
          )}
          <div className="flex justify-center">
            <Button
              type="submit"
              disabled={uploading}
              className="px-6 py-2 bg-violet-700 text-white rounded-md hover:bg-violet-800"
            >
              Upload
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}

export default VideoUpload;
