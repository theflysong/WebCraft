package cafe.qwq.webcraft.demo;

import cafe.qwq.webcraft.api.View;
import cafe.qwq.webcraft.api.WebScreen;
import cafe.qwq.webcraft.api.math.Vec4i;
import com.google.gson.JsonParser;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod("demo")
public class Demo
{
    public Demo()
    {
        MinecraftForge.EVENT_BUS.addListener(Demo::onGUiOpen);
        WebScreen.WebContainer.addSlotsClass(Slot.class,"slot");
    }

    public static void onGUiOpen(final GuiOpenEvent event)
    {
        if (event.getGui() instanceof MainMenuScreen)
        {
            //event.setGui(new DemoScreen());
            WebScreen screen = new WebScreen(new StringTextComponent("test"));
            View view = new View();
            view.setResizeCallback(vec -> new Vec4i(0, 0, vec.x, vec.y));
            try
            {
                view.loadHTML(new ResourceLocation("demo:test.html"));
                view.addJSFuncWithCallback("qwqwq", obj -> {
                    return new JsonParser().parse("{\"qwq\": \"老子宇宙第一可爱\"}");
                });
                view.addDOMReadyListener(() -> {
                    /*System.out.println(*/view.evaluteJS("qwq()");//.toString());
                });
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            //view.loadURL("https://qwq.cafe");
            //view.loadHTML("<p>我好可爱啊喵</p>");
            //view.loadHTML("<p style=\"font-family: 微软雅黑;color:#aaa;\">■■■</p>" +
            //        "<p style=\"font-family: 微软雅黑;color:#555;\">草草草</p>" +
            //        "<p style=\"font-family: 微软雅黑;color:#333;\">喵喵喵</p>" +
            //        "<p style=\"font-family: 微软雅黑;color:#777;\">■■■</p>");
            screen.addView(view).addPreRenderer((mouseX, mouseY, pTicks) -> screen.renderBackground());
            event.setGui(screen);
        }
    }

    public static void openGUIQAQ(final PlayerInteractEvent.LeftClickBlock event) {
        if(event.getUseBlock().equals(Blocks.DIAMOND_BLOCK)&&event.getUseItem().equals(Items.STICK)){
            WebScreen screen = new WebScreen(null,null,new StringTextComponent("test"));
            View view = new View();
            view.setResizeCallback(vec -> new Vec4i(0, 0, vec.x, vec.y));
            screen.addView(view).addPreRenderer((mouseX, mouseY, pTicks) -> screen.renderBackground());
            try {
                view.loadHTML(new ResourceLocation("demo:qaq.html"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            Minecraft.getInstance().displayGuiScreen(screen);
            //OpenGUI(我不会打开)
            //然后看看有没有什么地方可以放物品，我不会qaq
        }
    }
}
