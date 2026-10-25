package vn.hoanhap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int SPEECH = 701, AUDIO = 702;
    private static final int INK = Color.rgb(25,48,58), MUTED = Color.rgb(83,105,114);
    private static final int TEAL = Color.rgb(18,110,130), BG = Color.rgb(243,248,249);
    private TextToSpeech tts;
    private boolean ttsReady, autoSpeak;
    private EditText input;
    private TextView status;
    private LinearLayout library, favorites;
    private SharedPreferences prefs;
    private int textSize = 19;
    private float speechRate = 1f;
    private final LinkedHashMap<String,List<String>> groups = new LinkedHashMap<>();
    private final LinkedHashMap<String,String> icons = new LinkedHashMap<>();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("hoa_nhap_settings", MODE_PRIVATE);
        autoSpeak = prefs.getBoolean("auto_speak", false);
        textSize = prefs.getInt("text_size", 19);
        speechRate = prefs.getFloat("speech_rate", 1f);
        phrases();
        screen();
        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                int language = tts.setLanguage(new Locale("vi","VN"));
                ttsReady = language != TextToSpeech.LANG_MISSING_DATA && language != TextToSpeech.LANG_NOT_SUPPORTED;
                tts.setSpeechRate(speechRate);
                status.setText(ttsReady ? "Giọng đọc tiếng Việt đã sẵn sàng." : "Thiết bị chưa có dữ liệu giọng đọc tiếng Việt.");
            } else status.setText("Không khởi tạo được giọng đọc trên thiết bị.");
        });
    }

    private void phrases() {
        add("Nhu cầu cơ bản","💧","Tôi muốn uống nước.","Tôi muốn ăn.","Tôi cần đi vệ sinh.","Tôi muốn nghỉ ngơi.");
        add("Sức khỏe","🩺","Tôi cảm thấy không khỏe.","Tôi bị đau ở đây.","Tôi cần gặp bác sĩ.","Xin hãy gọi người nhà giúp tôi.");
        add("Giúp đỡ","🆘","Xin hãy giúp tôi.","Tôi chưa hiểu, hãy nói chậm hơn.","Xin hãy đợi một chút.","Tôi cần hỗ trợ.");
        add("Cảm xúc","💛","Tôi đang vui.","Tôi đang buồn.","Tôi đang lo lắng.","Tôi cần yên tĩnh.");
        add("Giao tiếp","💬","Xin chào!","Có.","Không.","Tôi chưa biết.","Cảm ơn bạn.","Xin lỗi.");
        add("Sinh hoạt","🏠","Tôi muốn về nhà.","Tôi muốn ra ngoài.","Tôi muốn nghe nhạc.","Tôi muốn gọi điện.");
        add("Ăn uống","🍚","Tôi muốn ăn cơm.","Tôi muốn uống sữa.","Tôi không muốn ăn món này.","Tôi bị dị ứng thực phẩm.");
        add("Khẩn cấp","🚨","Tôi đang gặp nguy hiểm.","Hãy gọi cấp cứu giúp tôi.","Tôi bị lạc.","Xin hãy liên hệ người thân của tôi.");
    }
    private void add(String name,String icon,String... values) {
        groups.put(name, Arrays.asList(values)); icons.put(name,icon);
    }

    private void screen() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        LinearLayout page = col(); page.setPadding(dp(18),dp(16),dp(18),dp(28)); scroll.addView(page); setContentView(scroll);
        LinearLayout hero = col(); hero.setPadding(dp(20),dp(20),dp(20),dp(20)); hero.setBackground(bg(TEAL,24)); page.addView(hero,match());
        hero.addView(label("GIAO TIẾP HÒA NHẬP",13,Color.WHITE,true));
        TextView title=label("Điều bạn muốn nói",29,Color.WHITE,true); title.setPadding(0,dp(8),0,dp(4)); hero.addView(title);
        hero.addView(label("Chọn câu có sẵn hoặc nhập lời nhắn. Điện thoại có thể đọc thành tiếng để giúp bạn giao tiếp.",16,Color.WHITE,false));

        LinearLayout heading=row(); heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.addView(label("Lời nhắn của bạn",21,INK,true),new LinearLayout.LayoutParams(0,dp(54),1));
        Button settings=button("⚙ Cài đặt",false); settings.setOnClickListener(v->settings()); heading.addView(settings); page.addView(heading);
        LinearLayout composer=col(); composer.setPadding(dp(14),dp(14),dp(14),dp(14)); composer.setBackground(bg(Color.WHITE,20));
        page.addView(composer,margin(0,dp(8),0,0));
        input=new EditText(this); input.setHint("Ví dụ: Tôi cần giúp đỡ..."); input.setTextSize(textSize);
        input.setTextColor(INK); input.setHintTextColor(MUTED); input.setGravity(Gravity.TOP|Gravity.START);
        input.setMinLines(2); input.setMaxLines(5); input.setPadding(dp(12),dp(12),dp(12),dp(12)); input.setBackground(bg(BG,12));
        input.setContentDescription("Nhập câu muốn nói"); composer.addView(input,match());
        LinearLayout actions=row();
        Button speak=button("🔊 Đọc câu",true); speak.setOnClickListener(v->speakCurrent());
        actions.addView(speak,new LinearLayout.LayoutParams(0,dp(56),1));
        Button listen=button("🎙 Nói",false); listen.setOnClickListener(v->startSpeech());
        LinearLayout.LayoutParams listenLp=new LinearLayout.LayoutParams(0,dp(56),1); listenLp.setMargins(dp(8),0,0,0); actions.addView(listen,listenLp);
        composer.addView(actions);
        LinearLayout shareRow=row();
        Button share=button("↗ Chia sẻ lời nhắn",false); share.setOnClickListener(v->share());
        shareRow.addView(share,new LinearLayout.LayoutParams(0,dp(52),1));
        Button clear=button("Xóa",false); clear.setOnClickListener(v->input.setText(""));
        LinearLayout.LayoutParams clearLp=new LinearLayout.LayoutParams(dp(84),dp(52)); clearLp.setMargins(dp(8),0,0,0); shareRow.addView(clear,clearLp);
        composer.addView(shareRow,margin(0,dp(8),0,0));
        status=label("Sẵn sàng hỗ trợ bạn.",14,MUTED,false); status.setPadding(dp(4),dp(10),dp(4),dp(12)); page.addView(status);
        page.addView(label("⭐ Câu yêu thích",22,INK,true)); page.addView(label("Các câu được lưu trên thiết bị này",14,MUTED,false));
        favorites=col(); page.addView(favorites,margin(0,dp(8),0,dp(18)));
        page.addView(label("🧩 Thư viện giao tiếp",22,INK,true)); page.addView(label("Chạm vào câu để chọn, ☆ để lưu yêu thích",14,MUTED,false));
        library=col(); page.addView(library,margin(0,dp(8),0,0));
        render();
    }

    private void render() {
        library.removeAllViews();
        for (Map.Entry<String,List<String>> group:groups.entrySet()) {
            LinearLayout card=col(); card.setPadding(dp(14),dp(12),dp(14),dp(10)); card.setBackground(bg(Color.WHITE,18));
            TextView h=label(icons.get(group.getKey())+"  "+group.getKey(),19,TEAL,true); h.setPadding(0,0,0,dp(8)); card.addView(h);
            for (String phrase:group.getValue()) {
                LinearLayout line=row(); line.setGravity(Gravity.CENTER_VERTICAL);
                TextView text=label("•  "+phrase,textSize,INK,false); text.setPadding(dp(4),dp(9),dp(4),dp(9));
                text.setContentDescription("Chọn câu: "+phrase); text.setOnClickListener(v->select(phrase));
                line.addView(text,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
                Button star=button(isFav(phrase)?"★":"☆",false); star.setTextSize(22);
                star.setContentDescription("Lưu hoặc bỏ yêu thích: "+phrase);
                star.setOnClickListener(v->{toggle(phrase);render();});
                line.addView(star); card.addView(line);
                View divider=new View(this); divider.setBackgroundColor(Color.rgb(230,238,240));
                card.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));
            }
            library.addView(card,margin(0,0,0,dp(12)));
        }
        favorites.removeAllViews();
        Set<String> saved=prefs.getStringSet("favorites",new HashSet<>());
        if(saved.isEmpty()) {
            TextView empty=label("Chưa có câu yêu thích. Chạm ☆ cạnh câu bất kỳ để lưu nhanh tại đây.",15,MUTED,false);
            empty.setPadding(dp(14),dp(14),dp(14),dp(14)); empty.setBackground(bg(Color.WHITE,16)); favorites.addView(empty); return;
        }
        for(String phrase:new ArrayList<>(saved)) {
            LinearLayout line=row(); line.setGravity(Gravity.CENTER_VERTICAL); line.setPadding(dp(12),dp(5),dp(5),dp(5)); line.setBackground(bg(Color.WHITE,14));
            TextView text=label(phrase,textSize,INK,false); text.setOnClickListener(v->select(phrase));
            line.addView(text,new LinearLayout.LayoutParams(0,-2,1));
            Button remove=button("★",false); remove.setOnClickListener(v->{toggle(phrase);render();}); line.addView(remove);
            favorites.addView(line,margin(0,0,0,dp(8)));
        }
    }

    private boolean isFav(String phrase) { return prefs.getStringSet("favorites",new HashSet<>()).contains(phrase); }
    private void toggle(String phrase) {
        Set<String> saved=new HashSet<>(prefs.getStringSet("favorites",new HashSet<>()));
        if(saved.contains(phrase)) saved.remove(phrase); else saved.add(phrase);
        prefs.edit().putStringSet("favorites",saved).apply();
    }
    private void select(String phrase) {
        input.setText(phrase); input.setSelection(input.length()); status.setText("Đã chọn câu. Nhấn “Đọc câu” để phát giọng nói.");
        if(autoSpeak) speak(phrase);
    }
    private void speakCurrent() {
        String phrase=input.getText().toString().trim();
        if(phrase.isEmpty()){Toast.makeText(this,"Hãy nhập hoặc chọn một câu trước nhé.",Toast.LENGTH_SHORT).show();return;}
        speak(phrase);
    }
    private void speak(String phrase) {
        if(tts==null||!ttsReady){status.setText("Giọng đọc chưa sẵn sàng. Hãy kiểm tra cài đặt TTS trên Android.");return;}
        if(Build.VERSION.SDK_INT>=21) tts.speak(phrase,TextToSpeech.QUEUE_FLUSH,null,"hoa-nhap");
        else tts.speak(phrase,TextToSpeech.QUEUE_FLUSH,null);
        status.setText("Đang đọc: "+phrase);
    }
    private void startSpeech() {
        if(Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},AUDIO); return;
        }
        launchRecognizer();
    }
    private void launchRecognizer() {
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"vi-VN"); intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hãy nói câu bạn muốn nhập");
        try { startActivityForResult(intent,SPEECH); }
        catch(ActivityNotFoundException e){status.setText("Chưa có ứng dụng nhận diện giọng nói. Bạn có thể nhập bằng bàn phím.");}
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(request,permissions,results);
        if(request==AUDIO) {
            if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED) launchRecognizer();
            else Toast.makeText(this,"Chưa cấp quyền micro. Bạn vẫn có thể nhập văn bản.",Toast.LENGTH_LONG).show();
        }
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request==SPEECH&&result==RESULT_OK&&data!=null) {
            ArrayList<String> words=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(words!=null&&!words.isEmpty()){input.setText(words.get(0));input.setSelection(input.length());status.setText("Đã nhận diện lời nói.");}
        }
    }
    private void share() {
        String phrase=input.getText().toString().trim();
        if(phrase.isEmpty()){Toast.makeText(this,"Hãy nhập hoặc chọn lời nhắn trước.",Toast.LENGTH_SHORT).show();return;}
        Intent send=new Intent(Intent.ACTION_SEND); send.setType("text/plain"); send.putExtra(Intent.EXTRA_TEXT,phrase);
        try{startActivity(Intent.createChooser(send,"Chia sẻ lời nhắn bằng ứng dụng"));}
        catch(ActivityNotFoundException e){status.setText("Không tìm thấy ứng dụng chia sẻ.");}
    }
    private void settings() {
        LinearLayout panel=col(); panel.setPadding(dp(20),dp(8),dp(20),dp(8));
        CheckBox auto=new CheckBox(this); auto.setText("Tự đọc câu khi tôi chọn"); auto.setTextSize(17); auto.setChecked(autoSpeak); panel.addView(auto);
        panel.addView(label("Cỡ chữ",17,INK,true));
        RadioGroup sizes=new RadioGroup(this); sizes.setOrientation(LinearLayout.VERTICAL);
        int[] sizeValues={17,19,23,27}; String[] sizeLabels={"Vừa","Lớn","Rất lớn","Cực lớn"};
        for(int i=0;i<sizeValues.length;i++){RadioButton b=new RadioButton(this);b.setText(sizeLabels[i]);b.setTextSize(17);b.setTag(sizeValues[i]);sizes.addView(b);if(textSize==sizeValues[i])b.setChecked(true);}
        panel.addView(sizes); panel.addView(label("Tốc độ đọc",17,INK,true));
        RadioGroup rates=new RadioGroup(this); rates.setOrientation(LinearLayout.VERTICAL);
        float[] rateValues={0.75f,1f,1.15f}; String[] rateLabels={"Chậm","Bình thường","Nhanh"};
        for(int i=0;i<rateValues.length;i++){RadioButton b=new RadioButton(this);b.setText(rateLabels[i]);b.setTextSize(17);b.setTag(rateValues[i]);rates.addView(b);if(Math.abs(speechRate-rateValues[i])<0.01)b.setChecked(true);}
        panel.addView(rates);
        new AlertDialog.Builder(this).setTitle("Cài đặt trợ năng").setView(panel).setNegativeButton("Hủy",null).setPositiveButton("Lưu",(d,w)->{
            autoSpeak=auto.isChecked(); RadioButton s=panelRadio(sizes); if(s!=null)textSize=(Integer)s.getTag();
            RadioButton r=panelRadio(rates); if(r!=null)speechRate=(Float)r.getTag();
            prefs.edit().putBoolean("auto_speak",autoSpeak).putInt("text_size",textSize).putFloat("speech_rate",speechRate).apply();
            if(tts!=null)tts.setSpeechRate(speechRate); input.setTextSize(textSize); render(); Toast.makeText(this,"Đã lưu cài đặt",Toast.LENGTH_SHORT).show();
        }).show();
    }
    private RadioButton panelRadio(RadioGroup group) {
        int id=group.getCheckedRadioButtonId(); if(id==-1)return null;
        for(int i=0;i<group.getChildCount();i++){View v=group.getChildAt(i);if(v.getId()==id&&v instanceof RadioButton)return (RadioButton)v;}return null;
    }
    private TextView label(String value,int size,int color,boolean bold) {
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
    }
    private Button button(String value,boolean primary) {
        Button b=new Button(this);b.setText(value);b.setTextSize(15);b.setAllCaps(false);b.setTextColor(primary?Color.WHITE:TEAL);
        b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setMinHeight(dp(48));b.setPadding(dp(8),dp(6),dp(8),dp(6));
        b.setBackground(bg(primary?TEAL:Color.rgb(231,243,245),14));b.setStateListAnimator(null);return b;
    }
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private LinearLayout.LayoutParams match(){return new LinearLayout.LayoutParams(-1,-2);}
    private LinearLayout.LayoutParams margin(int l,int t,int r,int b){LinearLayout.LayoutParams p=match();p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
