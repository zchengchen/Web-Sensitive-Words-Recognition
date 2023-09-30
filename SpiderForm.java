import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;
import java.net.URL;
import java.net.URLConnection;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class SpiderForm extends JFrame {
    //爬取内容显示区域
    private JTextArea jtaHtml = new JTextArea(15, 25);
    private JTextField jtfTargetWeb = new JTextField(25);
    private JTextArea jtaText = new JTextArea(15,25);
    private JTextArea jtaKeyWord = new JTextArea(8,25);
    //敏感词
    private ArrayList<String> keyWordList = new ArrayList<String>();
    private ArrayList<Integer> keyWordSum = new ArrayList<Integer>();

    public SpiderForm() throws IOException {
            this.setTitle("Java敏感词识别器");
            this.setLocation(300, 250);
            this.setSize(1200, 600);
            JPanel jpForAll = new JPanel();
            jpForAll.setLayout(new BorderLayout());
            //界面处理，提醒输入网址,爬取按钮
            JPanel jpNorthTarget = new JPanel();
            JLabel jlTargetWeb = new JLabel("爬取目标:");
            JButton jbSpiderTarget = new JButton("爬取");
            jpNorthTarget.setLayout(new BorderLayout());
            JScrollPane jspTargetWeb = new JScrollPane(jtfTargetWeb);
            jlTargetWeb.setPreferredSize(new Dimension(70,30));
            jspTargetWeb.setPreferredSize(new Dimension(300, 30));
            jpNorthTarget.add(jlTargetWeb ,BorderLayout.NORTH);
            jpNorthTarget.add(jspTargetWeb,BorderLayout.CENTER);
            jpNorthTarget.add(jbSpiderTarget,BorderLayout.EAST);
            //源代码文本,以及处理后的文本框设置
            jtaHtml.setEditable(false);
            jtaHtml.setLineWrap(true);
            //右侧敏感词词库和操作
            JPanel jpEastKeyWordArea = new JPanel();
            JButton jbLoadKeyWord = new JButton(" 加载敏感词库");
            JButton jbMatch = new JButton("敏感词识别");
            JButton jbLoadWebAddr = new JButton("加载网址库");
            JScrollPane jspKeyWord = new JScrollPane(jtaKeyWord);
            jpEastKeyWordArea.setLayout(new BorderLayout());
            jpEastKeyWordArea.add(jbLoadWebAddr,BorderLayout.NORTH);
            jpEastKeyWordArea.add(jbLoadKeyWord,BorderLayout.CENTER);
            jpEastKeyWordArea.add(jbMatch,BorderLayout.SOUTH);
            JPanel jplEastOp = new JPanel();
            jplEastOp.setLayout(new BorderLayout());
            jtaKeyWord.setLineWrap(true);
            jtaKeyWord.setEditable(false);
            jspKeyWord.setPreferredSize(new Dimension(5, 400));
            JLabel jlKeyWord = new JLabel("已导入的敏感词：");
            jplEastOp.add(jlKeyWord,BorderLayout.CENTER);
            jplEastOp.add(jpEastKeyWordArea,BorderLayout.NORTH);
            jplEastOp.add(jspKeyWord,BorderLayout.SOUTH);
            //文本显示区
            JPanel jpTitleHtml = new JPanel();
            JPanel jpTitleText = new JPanel();
            JLabel jlHtml = new JLabel();
            JLabel jlText = new JLabel();
            JScrollPane jspHtml = new JScrollPane(jtaHtml);
            JScrollPane jspText = new JScrollPane(jtaText);
            jlHtml.setText("Html源码：");
            jtaText.setEditable(false);
            jtaText.setLineWrap(true);
            jpTitleHtml.setLayout(new BorderLayout());
            jpTitleHtml.add(jlHtml,BorderLayout.NORTH);
            jpTitleHtml.add(jspHtml,BorderLayout.SOUTH);
            jlText.setText("文本提取/敏感词识别结果： ");
            jpTitleText.setLayout(new BorderLayout());
            jpTitleText.add(jlText,BorderLayout.NORTH);
            jpTitleText.add(jspText,BorderLayout.SOUTH);
            jspText.setPreferredSize(new Dimension(400,480));
            jspHtml.setPreferredSize(new Dimension(650,480));
            JPanel jpCenterResult = new JPanel();
            jpCenterResult.setLayout(new BorderLayout());
            jpCenterResult.add(jpTitleText,BorderLayout.EAST);
            jpCenterResult.add(jpTitleHtml,BorderLayout.WEST);
            JPanel jpSouthBlock = new JPanel();
            jpSouthBlock.setLayout(new BorderLayout());
            jpSouthBlock.add(jpCenterResult,BorderLayout.CENTER);
            jpSouthBlock.add(jplEastOp,BorderLayout.EAST);
            //南北块合并
            jpForAll.add(jpNorthTarget,BorderLayout.NORTH);
            jpForAll.add(jpSouthBlock,BorderLayout.CENTER);
            this.add(jpForAll);
            this.setVisible(true);
            //事件处理
            jbSpiderTarget.setActionCommand("Spider");
            jbLoadKeyWord.setActionCommand("LoadKW");
            jbMatch.setActionCommand("Match");
            jbLoadWebAddr.setActionCommand("LoadWA");
            ActionListener a1 = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(e.getActionCommand().equals("Spider")){
                        String webAddr = jtfTargetWeb.getText();
                        spiderOnlyOneWeb(webAddr);
                    }else if(e.getActionCommand().equals("LoadKW")){
                        getKeyWordLib();
                    }else if(e.getActionCommand().equals("Match")){
                        showKeyWord();
                    }else if(e.getActionCommand().equals("LoadWA")){
                        spiderByGroupThroughText();
                    }
                }
            };
            jbSpiderTarget.addActionListener(a1);
            jbLoadWebAddr.addActionListener(a1);
            jbLoadKeyWord.addActionListener(a1);
            jbMatch.addActionListener(a1);
    }

    //使用URL爬取网页的html代码
    public String getHtml(String webAddr) {
        String str = null;
        String htmlSource = "";
        try {
            URL url = new URL(webAddr);
            URLConnection urlConnected = url.openConnection();
            urlConnected.connect();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(urlConnected.getInputStream(),"UTF-8"));
            while(true) {
                str = buffer.readLine();
                if(str == null)	{
                    break;
                }
                htmlSource += (str + "\n");
            }
            buffer.close();
        }catch (Exception e) {
            JOptionPane.showMessageDialog(this, webAddr + " 爬取失败");
        }
        return htmlSource;
    }

    //对html进行正则匹配,提取出其中的文本
    public String getText(String str) {
        Pattern matchScriptTag = Pattern.compile(
                "<script[^>]*?>[\\s\\S]*?<\\/script>",
                Pattern.CASE_INSENSITIVE);
        Pattern matchMetaTag = Pattern.compile(
                "<meta[^>]*?>[\\s\\S]*?<\\/meta>",
                Pattern.CASE_INSENSITIVE);
        Pattern matchStyleTag = Pattern.compile(
                "<style[^>]*?>[\\s\\S]*?<\\/style>",
                Pattern.CASE_INSENSITIVE);
        Pattern matchRelinkTag = Pattern.compile(
                "<link[^>]*?>",
                Pattern.CASE_INSENSITIVE);
        Pattern matchOtherTag = Pattern.compile(
                "<[^>]+>",
                Pattern.CASE_INSENSITIVE);
        Pattern matchEscapeChar1 = Pattern.compile(
                "&nbsp;{1,}",
                Pattern.CASE_INSENSITIVE);
        Pattern matchEscapeChar2 = Pattern.compile(
                "&gt;{1,}"
                ,Pattern.CASE_INSENSITIVE);
        Pattern matchEscapeChar3 = Pattern.compile(
                "&lt;{1,}"
                ,Pattern.CASE_INSENSITIVE);
        Pattern matchEscapeChar4 = Pattern.compile(
                "&quot;{1,}"
                ,Pattern.CASE_INSENSITIVE);
        Pattern matchEscapeChar5 = Pattern.compile(
                "&copy;{1,}"
                ,Pattern.CASE_INSENSITIVE);
        Pattern matchEscapeChar6 = Pattern.compile(
                "&amp;{1,}"
                ,Pattern.CASE_INSENSITIVE);
        Pattern matchBlank = Pattern.compile(
                "[\\s]{2,}"
                , Pattern.CASE_INSENSITIVE);
        Matcher matcher = matchScriptTag.matcher(str);
        str = matcher.replaceAll("");
        matcher = matchMetaTag.matcher(str);
        str = matcher.replaceAll("");
        matcher = matchRelinkTag.matcher(str);
        str = matcher.replaceAll("");
        matcher = matchStyleTag.matcher(str);
        str = matcher.replaceAll("");
        matcher = matchOtherTag.matcher(str);
        str = matcher.replaceAll("");
        matcher = matchEscapeChar1.matcher(str);
        str = matcher.replaceAll("");
        matcher = matchEscapeChar2.matcher(str);
        str = matcher.replaceAll("");
        matcher = matchEscapeChar3.matcher(str);
        str = matcher.replaceAll("");
        matcher = matchEscapeChar4.matcher(str);
        str = matcher.replaceAll("");
        matcher = matchEscapeChar5.matcher(str);
        str = matcher.replaceAll("");
        matcher = matchEscapeChar6.matcher(str);
        str = matcher.replaceAll("");
        matcher = matchBlank.matcher(str);
        str = matcher.replaceAll("\n");
        str = str.substring(1,str.length());
        return str;
    }

    //从文件中读取敏感词
    public void getKeyWordLib() {
        JFileChooser jfChooser = new JFileChooser();
        int flag = jfChooser.showOpenDialog(this);
        if(flag != JFileChooser.APPROVE_OPTION){
            return;
        }
        keyWordList.clear();
        jtaKeyWord.setText("");
        File fileForKeyWord = jfChooser.getSelectedFile();
        BufferedReader buffer = null;
        try {
            buffer = new BufferedReader(new FileReader(fileForKeyWord));
            while(true) {
                String str = buffer.readLine();
                if(str == null){
                    break;
                }
                keyWordList.add(str);
                keyWordSum.add(0);
                jtaKeyWord.append(str + "\n");
            }
            buffer.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "文件读取失败！");
        }
    }

    //高亮显示
    public void showKeyWord() {
        Highlighter hg = jtaText.getHighlighter();
        hg.removeAllHighlights();
        String text = jtaText.getText();
        DefaultHighlightPainter painter = new DefaultHighlightPainter(Color.RED);
        for(int i = 0; i < keyWordList.size(); ++i) {
            int index = 0;
            while((index = text.indexOf(keyWordList.get(i),index)) >= 0) {
                try {
                    hg.addHighlight(index, index + keyWordList.get(i).length(), painter);
                    index += keyWordList.get(i).length();
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //爬取网址库中的网址
    public void spiderByGroupThroughText() {
        if(keyWordSum.size() <= 0) {
            JOptionPane.showMessageDialog(this, "请选择敏感词库");
            return;
        }
        JFileChooser fChooser = new JFileChooser();
        int flag = fChooser.showOpenDialog(this);
        if(flag != JFileChooser.APPROVE_OPTION){
            return;
        }
        File f = fChooser.getSelectedFile();
        spiderWebByGroup(f);
    }

    public void spiderOnlyOneWeb(String webAddr){
        if(webAddr.length() <= 0){
            JOptionPane.showMessageDialog(this,"网址不能为空！");
            return ;
        }
        jtaText.setText("");
        jtaHtml.setText("");
        String htmlSouce = getHtml(webAddr);
        String text = getText(htmlSouce);
        if(htmlSouce.length() > 0){
            jtaHtml.append(htmlSouce);
            jtaText.append(text);
        }else{
            JOptionPane.showMessageDialog(this,"爬取结束，但爬取网址内容为空。");
        }
    }

    public void spiderWebByGroup(File f){
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(f));
            PrintStream ps = new PrintStream(new File("result.txt"));
            int size = keyWordList.size();
            jtaText.setText("");
            jtaHtml.setText("");
            while(true) {
                String webAddr = buffer.readLine();
                if(webAddr == null || webAddr.trim().length() == 0){
                    break;
                }
                jtaHtml.append("网址： " + webAddr + "\n");
                jtaHtml.append(getHtml(webAddr) + "\n");
                jtaText.append("网址： " + webAddr + "\n");
                ps.println(webAddr + "敏感词识别结果: ");
                String html = getHtml(webAddr);
                String text = getText(html);
                for(int i = 0;i < size; ++i) {
                    String word = keyWordList.get(i);
                    int index = 0,cnt = 0,len = word.length();
                    ps.println("关键词： " + word);
                    while((index = text.indexOf(word,index)) >= 0) {
                        ++cnt;
                        int temp = keyWordSum.get(i);
                        keyWordSum.set(i,++temp);
                        index += len;
                        String output = "第 " + String.valueOf(cnt) +
                                " 次出现的索引为： " + String.valueOf(index) + "。";
                        ps.println(output);
                        jtaText.append(output + "\n");
                    }
                    String output1 = word + " " + cnt + "次 ";
                    ps.println(output1);
                    jtaText.append(output1 + "\n");
                }
                ps.println();
                jtaText.append("\n");
            }
            buffer.close();
            String output3 = "";
            ps.println("总计:     ");
            output3 += "总计:     ";
            output3 += "\n";
            for(int i = 0;i < size; i++) {
                String tmp = keyWordList.get(i) + " " + keyWordSum.get(i)+"次 ";
                ps.println(tmp);
                tmp += "\n";
                output3 += tmp;
            }
            ps.close();
            jtaText.append(output3);
            JOptionPane.showMessageDialog(this, "识别结束。");
        }catch (Exception e) {
            JOptionPane.showMessageDialog(this, "爬取源代码失败！");
        }
    }

    public static void main(String[] args) throws IOException {
        new SpiderForm();
    }
}