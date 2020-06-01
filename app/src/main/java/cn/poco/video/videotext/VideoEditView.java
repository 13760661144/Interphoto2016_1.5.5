package cn.poco.video.videotext;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import cn.poco.interphoto2.R;

/**
 * Created by lgd on 2017/7/17.
 */

public class VideoEditView extends EditText {
    public VideoEditView(Context context) {
        super(context);
        init();
    }

    private void init() {
        addTextChangedListener(mTextWatcher);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            CharSequence input = s.subSequence(start, start + count);
            if (count == 0) return;

            //如果 输入的类容包含有Emoji
            if (isEmojiCharacter(input)) {
                //那么就去掉
                Toast.makeText(getContext(), R.string.emoji_tip,Toast.LENGTH_SHORT).show();
                setText(removeEmoji(s));
//                setSelection(selection);
                setSelection(getText().length());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (onTextChange != null) {
                int index = -1;
                if (getTag() != null) {
                    index = (int) getTag();
                }
                String text = s.toString();
                if (isEmojiCharacter(text)) {
                    //那么就去掉
                    text = removeEmoji(text);
                }
                onTextChange.onChange(text, index);
            }
        }
    };

    /**
     * 去除字符串中的Emoji表情
     *
     * @param source
     * @return
     */
    private String removeEmoji(CharSequence source) {
        String result = "";
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (isEmojiCharacter(c)) {
                continue;
            }
            result += c;
        }
        return result;
    }

    /**
     * 判断一个字符串中是否包含有Emoji表情
     *
     * @param input
     * @return true 有Emoji
     */
    private boolean isEmojiCharacter(CharSequence input) {
        for (int i = 0; i < input.length(); i++) {
            if (isEmojiCharacter(input.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是Emoji 表情
     *
     * @param codePoint
     * @return true 是Emoji表情
     */
    public static boolean isEmojiCharacter(char codePoint) {
        // Emoji 范围
        boolean isScopeOf = (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD)
                || ((codePoint >= 0x20) && (codePoint <= 0xD7FF) && (codePoint != 0x263a))
                || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
                || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));

        return !isScopeOf;
    }

    private OnTextChange onTextChange;

    public void setOnTextChange(OnTextChange onTextChange) {
        this.onTextChange = onTextChange;
    }

    public interface OnTextChange {
        void onChange(String text, int tag);
    }
}
