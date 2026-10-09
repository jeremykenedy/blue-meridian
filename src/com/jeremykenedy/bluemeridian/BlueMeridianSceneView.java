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
            "varying vec2 vUv; uniform sampler2D uEarth; uniform float uTime; uniform float uAspect;" +
            "uniform float uSpin; uniform float uCamera; uniform float uStars; uniform float uAtmosphere;" +
            "uniform float uNightLights; uniform float uClouds;" +
            "float hash(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453123);}" +
            "float noise(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*(3.0-2.0*f);" +
            "return mix(mix(hash(i),hash(i+vec2(1.,0.)),f.x),mix(hash(i+vec2(0.,1.)),hash(i+vec2(1.,1.)),f.x),f.y);}" +
            "void main(){" +
            "vec2 cell=floor(gl_FragCoord.xy/6.0);float star=hash(cell);" +
            "float starShape=1.0-smoothstep(0.22,0.36,length(fract(gl_FragCoord.xy/6.0)-0.5));" +
            "float field=step(0.9988-uStars*0.0018,star)*starShape;" +
            "float twinkle=0.62+0.38*sin(uTime*1.6+star*60.0);" +
            "vec3 color=vec3(0.012,0.020,0.045)+field*twinkle*vec3(0.52,0.66,0.86);" +
            "float cx=0.51+sin(uTime*uCamera)*0.042;float cy=0.51+sin(uTime*uCamera*0.61+1.1)*0.018;" +
            "vec2 p=(vUv-vec2(cx,cy))*vec2(uAspect,1.0)/0.405;float r2=dot(p,p);" +
            "if(r2>1.0){if(uAtmosphere>0.5){float d=sqrt(r2);float glow=1.0-smoothstep(1.005,1.075,d);" +
            "color+=vec3(0.09,0.32,0.68)*glow*0.5;}gl_FragColor=vec4(color,1.0);return;}" +
            "float nz=sqrt(max(0.0,1.0-r2));vec3 n=normalize(vec3(p.x,-p.y,nz));" +
            "float angle=uTime*uSpin;float c=cos(angle),s=sin(angle);" +
            "vec3 world=vec3(c*n.x+s*n.z,n.y,-s*n.x+c*n.z);" +
            "float longitude=atan(world.x,world.z);float latitude=asin(clamp(world.y,-1.0,1.0));" +
            "vec2 uv=vec2(fract(longitude/6.2831853+0.5),0.5-latitude/3.14159265);" +
            "vec3 surface=texture2D(uEarth,uv).rgb;" +
            "vec3 lightDir=normalize(vec3(-0.42,0.30,0.86));float day=max(dot(world,lightDir),0.0);" +
            "float illumination=0.075+0.925*pow(day,0.72);" +
            "float night=1.0-smoothstep(-0.18,0.13,day);" +
            "float land=step(surface.b*1.08,surface.g)*step(0.11,surface.g);" +
            "float city=step(0.993,noise(uv*vec2(145.0,72.0)));" +
            "vec3 litSurface=surface*illumination;" +
            "if(uClouds>0.5){vec2 cloudUv=uv*vec2(43.0,22.0)+vec2(uTime*0.018,-uTime*0.006);" +
            "float cloud=0.55*noise(cloudUv)+0.30*noise(cloudUv*2.13)+0.15*noise(cloudUv*4.7);" +
            "float cover=smoothstep(0.58,0.73,cloud);litSurface=mix(litSurface,vec3(0.82,0.89,0.94)*illumination,cover*0.48);}" +
            "litSurface+=vec3(1.0,0.58,0.19)*city*land*night*uNightLights*0.74;" +
            "float edge=pow(1.0-nz,3.2)*uAtmosphere;litSurface+=vec3(0.12,0.42,0.95)*edge*1.8;" +
            "color=litSurface;gl_FragColor=vec4(color,1.0);}";
    private final Handler handler = new Handler();
    private final FloatBuffer vertices = ByteBuffer.allocateDirect(VERTICES.length * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer();
    private BlueMeridianOptions options;
    private int program;
    private int texture;
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
            texture = loadEarthTexture();
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
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
            uniform1i("uEarth", 0);
            uniform1f("uTime", (SystemClock.uptimeMillis() - startedAt) / 1000f);
            uniform1f("uAspect", width / (float) height);
            uniform1f("uSpin", options.spinSpeed);
            uniform1f("uCamera", options.cameraSpeed);
            uniform1f("uStars", options.stars / 260f);
            uniform1f("uAtmosphere", options.atmosphere ? 1f : 0f);
            uniform1f("uNightLights", options.nightLights ? 1f : 0f);
            uniform1f("uClouds", options.clouds ? 1f : 0f);
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

    private int loadEarthTexture() {
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.earth_surface, decodeOptions);
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
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
