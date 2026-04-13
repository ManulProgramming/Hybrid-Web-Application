from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from PIL import Image, ImageOps
import subprocess
import tempfile
import shutil
import os
import uvicorn
import tempfile
from pydantic import BaseModel
import uuid
import asyncio

app = FastAPI()

process_lock = asyncio.Lock()

origins = [
    "http://localhost",
    "http://localhost:8080",
    "http://localhost:5173"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class Item(BaseModel):
    path: str


def process_image(src, dst):
    with Image.open(src) as img:
        img = img.convert("RGB")
        cropped_img = ImageOps.fit(img, (512,512), Image.Resampling.BICUBIC, centering=(0.5, 0.5))
        cropped_img.save(dst, format="JPEG", quality=85)

def process_video(src, dst):
    cmd = [
        "ffmpeg",
        "-nostdin",
        "-y",
        "-i", src,
        "-vf", "pad=ceil(iw/2)*2:ceil(ih/2)*2",
        "-c:v", "libx264",
        "-preset", "fast",
        "-crf", "23",
        '-f', 'mp4',
        dst,
    ]
    subprocess.run(cmd, check=True)

def process_thumbnail(src, dst):
    cmd = [
        "ffmpeg",
        '-y',
        '-i', src,
        '-frames:v', '1',
        '-vf', "thumbnail",
        '-f', 'image2',
        '-vcodec', 'mjpeg',
        dst
    ]
    subprocess.run(cmd, check=True)

@app.post("/process_image")
async def processImage(item: Item):
    input_path = item.path

    output_tmp = tempfile.NamedTemporaryFile(delete=False,suffix=".out",dir="/app/shared")
    output_path = output_tmp.name
    output_tmp.close()

    async with process_lock:
        process_image(input_path, output_path)
    return {"path": output_path}


@app.post("/process_video")
async def processVideo(item: Item):
    input_path = item.path

    output_tmp = tempfile.NamedTemporaryFile(delete=False,suffix=".out",dir="/app/shared")
    output_path = output_tmp.name
    output_tmp.close()

    async with process_lock:
        process_video(input_path, output_path)
    return {"path": output_path}

@app.post("/create_thumbnail")
async def createThumbnail(item: Item):
    input_path = item.path

    output_tmp = tempfile.NamedTemporaryFile(delete=False,suffix=".out",dir="/app/shared")
    output_path = output_tmp.name
    output_tmp.close()

    async with process_lock:
        process_thumbnail(input_path, output_path)
    return {"path": output_path}

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)
