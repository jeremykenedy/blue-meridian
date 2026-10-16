package com.jeremykenedy.bluemeridian;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

final class BlueMeridianSceneView extends GLSurfaceView {
    private static final float[] VERTICES = {-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f};
    private static final String VERTEX_SHADER =
            "attribute vec2 aPosition; varying vec2 vUv; void main(){vUv=aPosition*0.5+0.5;gl_Position=vec4(aPosition,0.0,1.0);}";
    private static final String FRAGMENT_SHADER =
            "precision highp float;" +
            "varying vec2 vUv;" +
            "uniform sampler2D uEarth,uNight,uClouds;" +
            "uniform float uTime,uAspect,uSpin,uCamera,uStars,uAtmosphere,uNightLights,uCloudsEnabled;" +
            "float hash(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453123);}" +
            "void main(){" +
            "vec2 starPosition=gl_FragCoord.xy+vec2(uTime*0.14,uTime*0.08);vec2 cell=floor(starPosition/6.0);float star=hash(cell);" +
            "float starShape=1.0-smoothstep(0.22,0.36,length(fract(starPosition/6.0)-0.5));" +
            "float field=step(0.9988-uStars*0.0018,star)*starShape;" +
            "float twinkle=0.72+0.28*sin(uTime*0.7+star*60.0);" +
            "vec3 space=vec3(0.003,0.006,0.012)+field*twinkle*vec3(0.52,0.66,0.86);" +
            "float cx=0.51+sin(uTime*uCamera)*0.042;float cy=0.51+sin(uTime*uCamera*0.61+1.1)*0.018;" +
            "vec2 p=(vUv-vec2(cx,cy))*vec2(uAspect,1.0)/0.405;float r2=dot(p,p);" +
            "vec3 sun=normalize(vec3(-0.78,0.25,0.55));" +
            "if(r2>1.0){float glow=1.0-smoothstep(1.0,1.026,sqrt(r2));" +
            "vec3 limb=vec3(normalize(p),0.0);" +
            "float sunlit=clamp(dot(limb,sun)+0.35,0.05,1.0);" +
            "gl_FragColor=vec4(space+vec3(0.06,0.22,0.48)*glow*sunlit*uAtmosphere,1.0);return;}" +
            "float nz=sqrt(max(0.0,1.0-r2));vec3 n=normalize(vec3(p.x,p.y,nz));" +
            "float angle=uTime*uSpin;float c=cos(angle),s=sin(angle);" +
            "vec3 body=vec3(c*n.x+s*n.z,n.y,-s*n.x+c*n.z);" +
            "vec2 uv=vec2(fract(atan(body.x,body.z)/6.2831853+0.5),0.5-asin(clamp(body.y,-1.0,1.0))/3.14159265);" +
            "vec3 surface=texture2D(uEarth,uv).rgb;" +
            "float signedDay=dot(n,sun);float day=max(signedDay,0.0);" +
            "float illumination=0.035+1.25*pow(day,0.8);" +
            "vec3 color=pow(surface,vec3(2.2))*illumination;" +
            "float water=1.0-smoothstep(0.0,0.055,surface.g-surface.b*0.7);" +
            "color+=vec3(0.0015,0.006,0.016)*water*day;vec3 halfway=normalize(sun+vec3(0.0,0.0,1.0));" +
            "color+=vec3(0.78,0.88,1.0)*pow(max(dot(n,halfway),0.0),95.0)*water*0.16;" +
            "vec2 cloudUv=vec2(fract(uv.x+uTime*0.00035),uv.y);" +
            "float cloud=texture2D(uClouds,cloudUv).r*uCloudsEnabled;" +
            "float shadow=texture2D(uClouds,cloudUv+sun.xy*0.003).r*uCloudsEnabled;" +
            "color*=1.0-shadow*0.24;" +
            "color=mix(color,vec3(0.93,0.96,1.0)*illumination,cloud*0.88);" +
            "vec3 lights=texture2D(uNight,uv).rgb;float emission=max(lights.r-lights.b*0.9,0.0);" +
            "float night=1.0-smoothstep(-0.12,0.12,signedDay);" +
            "color+=pow(lights,vec3(2.2))*emission*night*uNightLights*(1.0-cloud*0.8)*2.0;" +
            "float rim=pow(1.0-nz,4.5)*uAtmosphere;" +
            "color+=vec3(0.025,0.11,0.28)*rim*clamp(signedDay+0.45,0.06,1.0);" +
            "color=pow(color,vec3(1.0/2.2));" +
            "gl_FragColor=vec4(color,1.0);" +
            "}";
    private final Handler handler = new Handler();
    private final FloatBuffer vertices = ByteBuffer.allocateDirect(VERTICES.length * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer();
    private BlueMeridianOptions options;
    private int program;
    private int[] textures;
    private int positionHandle;
    private long startedAt;
    private boolean running;

    BlueMeridianSceneView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        vertices.put(VERTICES).position(0);
        loadOptions();
        setRenderer(new PlanetRenderer());
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    void start() {
        if (running) return;
        super.onResume();
        running = true;
        startedAt = SystemClock.uptimeMillis();
        handler.post(frameRequest);
    }

    void stop() {
        running = false;
        handler.removeCallbacks(frameRequest);
        super.onPause();
    }

    private final Runnable frameRequest = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            requestRender();
            handler.postDelayed(this, 33L);
        }
    };

    private void loadOptions() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        options = BlueMeridianOptions.resolve(
                preferences.getString("star_density", "normal"),
                preferences.getString("spin_speed", "natural"),
                preferences.getString("camera_motion", "gentle"),
                preferences.getString("atmosphere", "on"),
                preferences.getString("night_lights", "on"),
                preferences.getString("clouds", "on"),
                preferences.getBoolean("randomize_all", false), new Random(System.currentTimeMillis()));
    }

    private final class PlanetRenderer implements Renderer {
        private int width;
        private int height;

        @Override
        public void onSurfaceCreated(GL10 ignored, EGLConfig config) {
            GLES20.glClearColor(0.01f, 0.02f, 0.04f, 1f);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
            textures = new int[] {loadTexture(R.drawable.earth_surface),
                    loadTexture(R.drawable.earth_night), loadTexture(R.drawable.earth_clouds)};
        }

        @Override
        public void onSurfaceChanged(GL10 ignored, int surfaceWidth, int surfaceHeight) {
            width = surfaceWidth;
            height = Math.max(1, surfaceHeight);
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 ignored) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUseProgram(program);
            vertices.position(0);
            GLES20.glEnableVertexAttribArray(positionHandle);
            GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertices);
            for (int unit = 0; unit < textures.length; unit++) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[unit]);
            }
            uniform1i("uEarth", 0);
            uniform1i("uNight", 1);
            uniform1i("uClouds", 2);
            uniform1f("uTime", (SystemClock.uptimeMillis() - startedAt) / 1000f);
            uniform1f("uAspect", width / (float) height);
            uniform1f("uSpin", options.spinSpeed);
            uniform1f("uCamera", options.cameraSpeed);
            uniform1f("uStars", options.stars / 260f);
            uniform1f("uAtmosphere", options.atmosphere ? 1f : 0f);
            uniform1f("uNightLights", options.nightLights ? 1f : 0f);
            uniform1f("uCloudsEnabled", options.clouds ? 1f : 0f);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glDisableVertexAttribArray(positionHandle);
        }

        private void uniform1f(String name, float value) {
            GLES20.glUniform1f(GLES20.glGetUniformLocation(program, name), value);
        }

        private void uniform1i(String name, int value) {
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program, name), value);
        }
    }

    private int loadTexture(int resource) {
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inScaled = false;
        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resource, decodeOptions);
        int[] maximum = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maximum, 0);
        decodeOptions.inSampleSize = 1;
        while (decodeOptions.outWidth / decodeOptions.inSampleSize > maximum[0]
                || decodeOptions.outHeight / decodeOptions.inSampleSize > maximum[0]) {
            decodeOptions.inSampleSize *= 2;
        }
        decodeOptions.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resource, decodeOptions);
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        bitmap.recycle();
        return textures[0];
    }

    private static int createProgram(String vertexSource, String fragmentSource) {
        int vertex = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragment = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertex);
        GLES20.glAttachShader(shaderProgram, fragment);
        GLES20.glLinkProgram(shaderProgram);
        int[] status = new int[1];
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] == 0) throw new IllegalStateException(GLES20.glGetProgramInfoLog(shaderProgram));
        GLES20.glDeleteShader(vertex);
        GLES20.glDeleteShader(fragment);
        return shaderProgram;
    }

    private static int compileShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) throw new IllegalStateException(GLES20.glGetShaderInfoLog(shader));
        return shader;
    }
}
