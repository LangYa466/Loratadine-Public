package shop.xmz.lol.loratadine.utils.font;

import shop.xmz.lol.loratadine.Loratadine;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;

public class FontManager {
    public static Font defRegular;
    public TrueTypeFont ax12;
    public TrueTypeFont ax14;
    public TrueTypeFont ax18;
    public TrueTypeFont ax20;
    public TrueTypeFont tenacity;
    public TrueTypeFont tenacity20;
    public TrueTypeFont tenacity18;
    public TrueTypeFont tenacity16;
    public TrueTypeFont tenacity14;
    public TrueTypeFont tenacityBold14;
    public TrueTypeFont tenacityBold15;
    public TrueTypeFont tenacityBold16;
    public TrueTypeFont tenacityBold18;
    public TrueTypeFont tenacityBold20;
    public TrueTypeFont tenacityBold22;
    public TrueTypeFont tenacityBold60;
    public TrueTypeFont zw12;
    public TrueTypeFont zw14;
    public TrueTypeFont zw16;
    public TrueTypeFont zw18;
    public TrueTypeFont zw19;
    public TrueTypeFont zw20;
    public TrueTypeFont zw22;
    public TrueTypeFont zw26;
    public TrueTypeFont zw30;
    public TrueTypeFont icon18;
    public TrueTypeFont icon25;
    public TrueTypeFont icon30;
    public TrueTypeFont icon33;
    public TrueTypeFont icon45;
    public TrueTypeFont borel40;
    public TrueTypeFont borel60;
    public TrueTypeFont PlymouthRock60;
    public TrueTypeFont Plymouth60;
    public TrueTypeFont Grid40;
    public TrueTypeFont Grid60;

    public void init() {
        defRegular = new Font("微软雅黑", Font.PLAIN, 72);
        tenacity = createFontRenderer(getFont(25, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\simsun.ttf")));
        icon45 = createFontRenderer(getFont(45, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\icons.ttf")));
        icon33 = createFontRenderer(getFont(33, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\icons.ttf")));
        icon30 = createFontRenderer(getFont(30, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\icons.ttf")));
        icon25 = createFontRenderer(getFont(25, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\icons.ttf")));
        icon18 = createFontRenderer(getFont(18, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\icons.ttf")));
        ax12 = createFontRenderer(getFont(12, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\Axiforma.ttf")));
        ax14 = createFontRenderer(getFont(14, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\Axiforma.ttf")));
        ax18 = createFontRenderer(getFont(18, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\Axiforma.ttf")));
        ax20 = createFontRenderer(getFont(20, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\Axiforma.ttf")));
        zw30 = createFontRenderer(getFont(30, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\misans.ttf")));
        zw20 = createFontRenderer(getFont(20, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\misans.ttf")));
        zw18 = createFontRenderer(getFont(18, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\misans.ttf")));
        zw19 = createFontRenderer(getFont(19, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\misans.ttf")));
        zw16 = createFontRenderer(getFont(16, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\misans.ttf")));
        zw26 = createFontRenderer(getFont(26, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\misans.ttf")));
        zw22 = createFontRenderer(getFont(22, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\misans.ttf")));
        zw14 = createFontRenderer(getFont(14, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\misans.ttf")));
        zw12 = createFontRenderer(getFont(12, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\Axiforma.ttf")));
        tenacity14 = createFontRenderer(getFont(14, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\tenacity.ttf")));
        tenacity16 = createFontRenderer(getFont(16, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\tenacity.ttf")));
        tenacity18 = createFontRenderer(getFont(18, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\tenacity.ttf")));
        tenacity20 = createFontRenderer(getFont(20, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\tenacity.ttf")));
        tenacityBold14 = createFontRenderer(getFont(14, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\tenacity-bold.ttf")));
        tenacityBold15 = createFontRenderer(getFont(15, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\tenacity-bold.ttf")));
        tenacityBold16 = createFontRenderer(getFont(16, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\tenacity-bold.ttf")));
        tenacityBold18 = createFontRenderer(getFont(18, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\tenacity-bold.ttf")));
        tenacityBold20 = createFontRenderer(getFont(20, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\tenacity-bold.ttf")));
        tenacityBold22 = createFontRenderer(getFont(22, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\tenacity-bold.ttf")));
        tenacityBold60 = createFontRenderer(getFont(80, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\tenacity-bold.ttf")));
        PlymouthRock60 = createFontRenderer(getFont(60, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\PlymouthRock.ttf")));
        Grid40 = createFontRenderer(getFont(40, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\Grid.ttf")));
        Grid60 = createFontRenderer(getFont(60, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\Grid.ttf")));
        borel60 = createFontRenderer(getFont(60, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\Borel Regular.ttf")));
        Plymouth60 = createFontRenderer(getFont(60, new File(Loratadine.INSTANCE.getResourcesManager().font.getAbsolutePath() + "\\PlymouthRock.ttf")));
        System.out.println("Load font file successful.");
    }

    public static Font driveFont(Font font, int style, int size) {
        return font.deriveFont(style, size);
    }

    public Font getFont(int size, File file) {
        Font font;
        try (FileInputStream fis = new FileInputStream(file)) {
            font = Font.createFont(Font.TRUETYPE_FONT, fis).deriveFont(Font.PLAIN, (float) size);
        } catch (Exception ex) {
            ex.printStackTrace();
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }

    public TrueTypeFont createFontRenderer(Font font) {
        return new TrueTypeFont(font, true, false);
    }
}
