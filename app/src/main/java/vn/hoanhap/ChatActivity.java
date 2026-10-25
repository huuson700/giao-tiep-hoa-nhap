package vn.hoanhap;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.security.SecureRandom;
import java.util.*;

public class ChatActivity extends Activity {
    private static final int TEAL=Color.rgb(18,110,130), INK=Color.rgb(25,48,58), MUTED=Color.rgb(83,105,114), BG=Color.rgb(243,248,249);
    private FirebaseApp firebaseApp;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ListenerRegistration messageListener;
    private String roomId="", roomTitle="";
    private LinearLayout page, content, messages;
    private ScrollView messageScroll;
    private TextView status;
    private EditText emailField, passwordField, nameField, codeField, messageField;
    private boolean signUpMode=false;
    private final SecureRandom random=new SecureRandom();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if(!configureFirebase()) { showFirebaseSetup(); return; }
        auth=FirebaseAuth.getInstance(firebaseApp);
        db=FirebaseFirestore.getInstance(firebaseApp);
        user=auth.getCurrentUser();
        if(user==null) showAuth(); else showRooms();
    }

    private boolean configureFirebase() {
        try {
            for(FirebaseApp app:FirebaseApp.getApps(this)) if("hoa-nhap".equals(app.getName())) firebaseApp=app;
            if(firebaseApp!=null) return true;
            android.content.SharedPreferences p=getSharedPreferences("firebase_config",MODE_PRIVATE);
            String api=p.getString("apiKey",""), appId=p.getString("appId",""), project=p.getString("projectId","");
            if(api.isEmpty()||appId.isEmpty()||project.isEmpty()) return false;
            FirebaseOptions options=new FirebaseOptions.Builder().setApiKey(api).setApplicationId(appId).setProjectId(project).build();
            firebaseApp=FirebaseApp.initializeApp(this,options,"hoa-nhap");
            return firebaseApp!=null;
        } catch(Exception e) { return false; }
    }

    private void showFirebaseSetup() {
        base("Thiết lập nhắn tin trực tuyến");
        addText(content,"Kết nối Firebase",25,INK,true);
        addText(content,"Cần cấu hình Firebase của dự án để đăng nhập và đồng bộ tin nhắn. Thông tin này không phải mật khẩu tài khoản.",15,MUTED,false);
        EditText api=field("Firebase Web API Key",false);
        EditText appId=field("Firebase App ID",false);
        EditText project=field("Firebase Project ID",false);
        content.addView(api,margin(0,dp(12),0,0)); content.addView(appId,margin(0,dp(8),0,0)); content.addView(project,margin(0,dp(8),0,0));
        Button save=button("Lưu cấu hình và tiếp tục",true);
        save.setOnClickListener(v->{
            String a=api.getText().toString().trim(), b=appId.getText().toString().trim(), c=project.getText().toString().trim();
            if(a.isEmpty()||b.isEmpty()||c.isEmpty()){toast("Hãy nhập đủ 3 giá trị cấu hình.");return;}
            getSharedPreferences("firebase_config",MODE_PRIVATE).edit().putString("apiKey",a).putString("appId",b).putString("projectId",c).apply();
            if(configureFirebase()){auth=FirebaseAuth.getInstance(firebaseApp);db=FirebaseFirestore.getInstance(firebaseApp);showAuth();}
            else {toast("Cấu hình chưa hợp lệ. Hãy kiểm tra lại trong Firebase Console.");}
        });
        content.addView(save,margin(0,dp(14),0,0));
        Button guide=button("Hướng dẫn thiết lập",false);
        guide.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Thiết lập Firebase").setMessage(
            "1. Tạo project tại console.firebase.google.com.\n"+
            "2. Thêm Android app với package name vn.hoanhap.\n"+
            "3. Authentication → Sign-in method → bật Email/Password.\n"+
            "4. Tạo Firestore Database.\n"+
            "5. Project settings → General → Your apps → lấy Web API Key, App ID và Project ID.\n"+
            "6. Dán 3 giá trị vào đây.\n"+
            "7. Cài Firestore Security Rules từ tệp firestore.rules trong repository.\n\n"+
            "Không công khai service-account key. API key Firebase dành cho client không thay thế Security Rules.")
            .setPositiveButton("Đã hiểu",null).show());
        content.addView(guide,margin(0,dp(8),0,0));
        addText(content,"Ứng dụng chưa gửi dữ liệu cho tới khi bạn nhập cấu hình Firebase.",14,MUTED,false);
    }

    private void showAuth() {
        base("Đăng nhập");
        addText(content,"Trò chuyện an toàn hơn, cùng nhau",25,INK,true);
        addText(content,"Đăng nhập bằng email để tạo phòng riêng hoặc tham gia bằng mã mời.",15,MUTED,false);
        emailField=field("Email",false); emailField.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        passwordField=field("Mật khẩu (ít nhất 6 ký tự)",true); passwordField.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        content.addView(emailField,margin(0,dp(16),0,0)); content.addView(passwordField,margin(0,dp(8),0,0));
        Button primary=button("Đăng nhập",true); primary.setOnClickListener(v->authenticate());
        content.addView(primary,margin(0,dp(12),0,0));
        Button switchMode=button("Tạo tài khoản mới",false); switchMode.setOnClickListener(v->{signUpMode=!signUpMode;showAuth();});
        content.addView(switchMode,margin(0,dp(8),0,0));
        Button reset=button("Quên mật khẩu",false); reset.setOnClickListener(v->resetPassword());
        content.addView(reset,margin(0,dp(8),0,0));
        addText(content,"Dùng mật khẩu riêng cho ứng dụng. Không gửi mật khẩu cho người khác.",13,MUTED,false);
    }

    private void authenticate() {
        String email=emailField.getText().toString().trim(), password=passwordField.getText().toString();
        if(email.isEmpty()||password.length()<6){toast("Nhập email hợp lệ và mật khẩu ít nhất 6 ký tự.");return;}
        status.setText("Đang kết nối...");
        if(signUpMode) auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this,t->{
            if(t.isSuccessful()){user=auth.getCurrentUser();showRooms();}
            else showError("Không tạo được tài khoản",t.getException());
        });
        else auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this,t->{
            if(t.isSuccessful()){user=auth.getCurrentUser();showRooms();}
            else showError("Đăng nhập thất bại",t.getException());
        });
    }
    private void resetPassword() {
        String email=emailField.getText().toString().trim();
        if(email.isEmpty()){toast("Nhập email trước để nhận liên kết đặt lại mật khẩu.");return;}
        auth.sendPasswordResetEmail(email).addOnCompleteListener(this,t->{if(t.isSuccessful())toast("Nếu email được đăng ký, hướng dẫn đặt lại mật khẩu sẽ được gửi.");else showError("Không gửi được email đặt lại",t.getException());});
    }

    private void showRooms() {
        user=auth.getCurrentUser(); if(user==null){showAuth();return;}
        base("Tin nhắn trực tuyến");
        addText(content,"Xin chào!",24,INK,true);
        addText(content,user.getEmail()==null?"Đã đăng nhập":user.getEmail(),14,MUTED,false);
        addText(content,"Tạo một phòng riêng rồi chia sẻ mã mời với người cần trò chuyện. Ai có mã mời đều có thể yêu cầu tham gia, vì vậy chỉ chia sẻ với người bạn tin tưởng.",15,INK,false);
        Button create=button("＋ Tạo phòng trò chuyện",true); create.setOnClickListener(v->createRoom());
        content.addView(create,margin(0,dp(16),0,0));
        addText(content,"Tham gia phòng bằng mã mời",19,INK,true);
        codeField=field("Mã mời gồm 8 ký tự",false); codeField.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        content.addView(codeField,margin(0,dp(8),0,0));
        Button join=button("Tham gia phòng",false); join.setOnClickListener(v->joinRoom());
        content.addView(join,margin(0,dp(8),0,0));
        Button logout=button("Đăng xuất",false); logout.setOnClickListener(v->{auth.signOut();user=null;showAuth();});
        content.addView(logout,margin(0,dp(16),0,0));
        addText(content,"Phòng bạn đã tham gia có thể mở lại bằng mã mời.",14,MUTED,false);
    }

    private void createRoom() {
        EditText name=field("Tên phòng (ví dụ: Gia đình)",false);
        new AlertDialog.Builder(this).setTitle("Tạo phòng mới").setView(name).setNegativeButton("Hủy",null).setPositiveButton("Tạo",(d,w)->{
            String title=name.getText().toString().trim(); if(title.isEmpty())title="Phòng trò chuyện";
            String id=db.collection("rooms").document().getId(), code=makeCode();
            Map<String,Object> room=new HashMap<>(); room.put("title",title); room.put("ownerId",user.getUid()); room.put("memberIds",Arrays.asList(user.getUid())); room.put("createdAt",FieldValue.serverTimestamp());
            db.collection("rooms").document(id).set(room).continueWithTask(task->{
                if(!task.isSuccessful())throw task.getException();
                Map<String,Object> invite=new HashMap<>(); invite.put("roomId",id); invite.put("createdBy",user.getUid()); invite.put("createdAt",FieldValue.serverTimestamp());
                return db.collection("invites").document(code).set(invite);
            }).addOnSuccessListener(v->{
                new AlertDialog.Builder(this).setTitle("Phòng đã được tạo").setMessage("Tên: "+title+"\nMã mời: "+code+"\n\nHãy chia sẻ mã này riêng tư với người muốn trò chuyện.")
                    .setPositiveButton("Mở phòng",(di,wi)->openRoom(id,title,code)).setNegativeButton("Đóng",(di,wi)->showRooms()).show();
            }).addOnFailureListener(e->showError("Không tạo được phòng",e));
        }).show();
    }

    private String makeCode() {
        final String alphabet="ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder b=new StringBuilder(); for(int i=0;i<8;i++)b.append(alphabet.charAt(random.nextInt(alphabet.length())));return b.toString();
    }

    private void joinRoom() {
        String code=codeField.getText().toString().trim().toUpperCase(Locale.ROOT);
        if(code.length()!=8){toast("Mã mời cần đủ 8 ký tự.");return;}
        db.collection("invites").document(code).get().addOnSuccessListener(invite->{
            if(!invite.exists()){toast("Không tìm thấy mã mời. Hãy kiểm tra lại.");return;}
            String id=invite.getString("roomId"); if(id==null){toast("Mã mời không hợp lệ.");return;}
            db.collection("rooms").document(id).get().addOnSuccessListener(room->{
                if(!room.exists()){toast("Phòng này không còn tồn tại.");return;}
                String title=room.getString("title"); if(title==null)title="Phòng trò chuyện";
                db.collection("rooms").document(id).update("memberIds",FieldValue.arrayUnion(user.getUid()))
                    .addOnSuccessListener(v->{toast("Đã tham gia phòng.");openRoom(id,title,code);})
                    .addOnFailureListener(e->showError("Không tham gia được phòng",e));
            }).addOnFailureListener(e->showError("Không đọc được phòng",e));
        }).addOnFailureListener(e->showError("Không kiểm tra được mã mời",e));
    }

    private void openRoom(String id,String title,String code) {
        roomId=id; roomTitle=title; base(title);
        Button back=button("← Danh sách phòng",false);back.setOnClickListener(v->{detachMessages();showRooms();});
        content.addView(back,margin(0,0,0,dp(8)));
        TextView invite=addText(content,"Mã mời: "+code+"  ·  Chia sẻ mã này riêng tư",14,TEAL,true);
        invite.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Mã mời").setMessage(code).setPositiveButton("Đã hiểu",null).show());
        messageScroll=new ScrollView(this);messageScroll.setFillViewport(false);messageScroll.setBackground(bg(Color.WHITE,16));
        messages=col();messages.setPadding(dp(10),dp(10),dp(10),dp(10));messageScroll.addView(messages);
        LinearLayout.LayoutParams scrollLp=new LinearLayout.LayoutParams(-1,0,1);scrollLp.setMargins(0,dp(8),0,dp(8));content.addView(messageScroll,scrollLp);
        LinearLayout compose=row();messageField=field("Nhập tin nhắn…",false);messageField.setTextSize(16);
        compose.addView(messageField,new LinearLayout.LayoutParams(0,dp(54),1));
        Button send=button("Gửi",true);send.setOnClickListener(v->sendMessage());compose.addView(send);
        content.addView(compose);
        listenMessages();
    }

    private void listenMessages() {
        detachMessages();
        messageListener=db.collection("rooms").document(roomId).collection("messages").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(200)
            .addSnapshotListener((snap,error)->{
                if(error!=null){status.setText("Lỗi đồng bộ: "+error.getLocalizedMessage());return;}
                if(snap==null)return; messages.removeAllViews();
                for(DocumentSnapshot doc:snap.getDocuments()) {
                    String sender=doc.getString("senderEmail");if(sender==null)sender="Người tham gia";
                    String body=doc.getString("text");if(body==null)body="";
                    java.util.Date date=doc.getDate("createdAt");
                    String time=date==null?"":new java.text.SimpleDateFormat("HH:mm",new Locale("vi","VN")).format(date);
                    LinearLayout bubble=col();bubble.setPadding(dp(10),dp(8),dp(10),dp(8));
                    boolean mine=user.getUid().equals(doc.getString("senderId"));
                    bubble.setBackground(bg(mine?Color.rgb(220,243,246):BG,14));
                    bubble.addView(addText(bubble,(mine?"Bạn":sender)+"  "+time,12,MUTED,true));
                    addText(bubble,body,17,INK,false);
                    LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.setMargins(0,0,0,dp(7));messages.addView(bubble,bp);
                }
                messageScroll.post(()->messageScroll.fullScroll(View.FOCUS_DOWN));
                status.setText("Đã đồng bộ tin nhắn.");
            });
    }

    private void sendMessage() {
        String text=messageField.getText().toString().trim();
        if(text.isEmpty())return;
        if(text.length()>1000){toast("Tin nhắn tối đa 1.000 ký tự.");return;}
        Map<String,Object> msg=new HashMap<>();msg.put("text",text);msg.put("senderId",user.getUid());
        msg.put("senderEmail",user.getEmail()==null?"Người dùng":user.getEmail());msg.put("createdAt",FieldValue.serverTimestamp());
        db.collection("rooms").document(roomId).collection("messages").add(msg).addOnSuccessListener(r->{
            messageField.setText("");((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(messageField.getWindowToken(),0);
        }).addOnFailureListener(e->showError("Không gửi được tin nhắn",e));
    }

    private void detachMessages(){if(messageListener!=null){messageListener.remove();messageListener=null;}}
    private void base(String title) {
        LinearLayout root=col();root.setBackgroundColor(BG);root.setPadding(dp(18),dp(16),dp(18),dp(20));
        TextView h=addText(root,"GIAO TIẾP HÒA NHẬP  ·  "+title,20,TEAL,true);
        h.setPadding(0,0,0,dp(12));
        ScrollView outer=new ScrollView(this);outer.setFillViewport(true);outer.addView(root);setContentView(outer);
        content=col();content.setPadding(dp(16),dp(16),dp(16),dp(16));content.setBackground(bg(Color.WHITE,20));root.addView(content,new LinearLayout.LayoutParams(-1,-1));
        status=addText(content,"Kết nối dữ liệu qua Firebase",13,MUTED,false);status.setPadding(0,dp(10),0,0);
    }
    private EditText field(String hint,boolean password) {
        EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(!hint.startsWith("Tên phòng"));e.setTextSize(16);e.setTextColor(INK);e.setHintTextColor(MUTED);
        e.setPadding(dp(12),dp(10),dp(12),dp(10));e.setBackground(bg(BG,12));return e;
    }
    private TextView addText(LinearLayout parent,String value,int size,int color,boolean bold) {
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);parent.addView(t,new LinearLayout.LayoutParams(-1,-2));return t;
    }
    private Button button(String value,boolean primary) {
        Button b=new Button(this);b.setText(value);b.setTextSize(15);b.setAllCaps(false);b.setTextColor(primary?Color.WHITE:TEAL);
        b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setMinHeight(dp(48));b.setPadding(dp(8),dp(6),dp(8),dp(6));b.setBackground(bg(primary?TEAL:Color.rgb(231,243,245),14));b.setStateListAnimator(null);return b;
    }
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private LinearLayout.LayoutParams margin(int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private void toast(String text){Toast.makeText(this,text,Toast.LENGTH_LONG).show();}
    private void showError(String title,Exception e){String message=e==null?"":e.getLocalizedMessage();new AlertDialog.Builder(this).setTitle(title).setMessage(message==null?"Có lỗi xảy ra.":message).setPositiveButton("Đóng",null).show();}
    @Override protected void onDestroy(){detachMessages();super.onDestroy();}
}
