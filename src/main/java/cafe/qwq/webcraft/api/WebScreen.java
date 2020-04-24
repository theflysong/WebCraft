package cafe.qwq.webcraft.api;

import cafe.qwq.webcraft.api.math.Vec2i;
import cafe.qwq.webcraft.api.math.Vec4i;
import cafe.qwq.webcraft.client.KeyboardHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.ItemStackHandler;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class WebScreen<T extends Container> extends ContainerScreen
{
    private List<View> viewList;
    private boolean shouldCloseOnEsc;
    private List<IRenderer> rendererList1;
    private List<IRenderer> rendererList2;
    protected double scale;
    private static int id=50;

    public static class WebContainer extends Container {
        private Map<String,Slot> slots = new HashMap();
        private List<SlotInfo> slotInfos = new ArrayList();
        private static Map<String, Class<? extends Slot>> slotTypes = new HashMap();
        private int index = 1;
        private IInventory inv;
        private ItemStackHandler items;

        public class SlotInfo{
            public String slotName;
            public String slotType;
            public Vec2i Location;

            /**
             * @param slotName 物品槽名
             * @param slotType 物品槽类型
             * @param Location 物品槽位置
             */
            public SlotInfo(String slotName,String slotType,Vec2i Location){
                this.slotName=slotName;
                this.slotType=slotType;
                this.Location=Location;

            }

            /**
             * @param slotName 物品槽名
             * @param slotType 物品槽类型
             * @param x 物品槽x轴位置
             * @param y 物品槽y轴位置
             */
            public SlotInfo(String slotName,String slotType,int x,int y){
                this(slotName,slotType,new Vec2i(x,y));
            }
        }

        public static void addSlotsClass(Class<? extends Slot> type,String typeName){
            slotTypes.put(typeName,type);
        }

        protected WebContainer(@Nullable ContainerType<?> type, int id,IInventory inv) {
            super(type, id);
            this.inv=inv;
        }

        /**
         * @param s 物品槽
         * @param name 物品槽名字
         * @return 用于链式
         */
        public WebContainer addSlot(Slot s,String name){
            this.slots.put(name,s);
            return this;
        }

        /**
         * @param info 物品槽信息
         * @return 用于链式
         */
        public WebContainer addSlotFromInfo(SlotInfo info){
            this.slotInfos.add(info);
            return this;
        }

        private String getString(int where,String s){
            String style="";
            boolean isSlot=false;
            char ch=' ';
            for(int j=where;j<s.length();j++){
                ch=s.charAt(j);
                if(ch=='"'){
                    break;
                }
                else if(ch==';'||ch==':'){
                    return style;
                }
                style+=ch;
            }
            return "String End!";
        }

        /**
         * 不用调用，会自己调用
         * @param path 文件路径
         * @throws FileNotFoundException 不做表述
         */
        public void addItemSlot(String path) throws FileNotFoundException {
            File f = new File(path);
            Scanner scan = new Scanner(f);
            String code = "";
            while (scan.hasNext()){
                code += scan.nextLine();
            }
            boolean isDiv = false;
            int where = -1;
            for(int k=0;k<code.length();k++){
                for (int i = k; i < code.length(); i++) {
                    if (isDiv) {
                        if (code.charAt(i) == 's') {
                            if (code.charAt(i) == 't') {
                                if (code.charAt(++i) == 'y') {
                                    if (code.charAt(++i) == 'l') {
                                        if (code.charAt(i) == 'y') {
                                            where = ++i;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (code.charAt(i) == 'd') {
                            if (code.charAt(++i) == 'i') {
                                if (code.charAt(++i) == 'v') {
                                    isDiv = true;
                                }
                            }
                        }
                    }
                }
                char ch='=';
                if (where != -1) {
                    for(int j=where;j<code.length();j++){
                        if(code.charAt(j)==' '||code.charAt(j)==ch){
                            if(code.charAt(j)==ch){
                                if(ch=='='){
                                    ch='"';
                                }
                                if(ch=='"'){
                                    where=j;
                                    break;
                                }
                            }
                        }
                        else{
                            break;
                        }
                    }
                    String style="";
                    boolean isSlot=false;
                    int xLocaltion=0,yLocaltion=0;
                    String SlotType="",SlotName="";
                    while(true){
                        style=getString(where,code);
                        where+=style.length()+1;
                        if(style=="String End!"){
                            break;
                        }
                        if(isSlot){
                            switch(style){
                                case "x":
                                    xLocaltion = Integer.valueOf(getString(where,code));
                                    where+=style.length()+1;
                                    break;
                                case "y":
                                    yLocaltion = Integer.valueOf(getString(where,code));
                                    where+=style.length()+1;
                                    break;
                                case "type":
                                    SlotType = getString(where,code);
                                    where+=style.length()+1;
                                    break;
                                case "name":
                                    SlotName = getString(where,code);
                                    where+=style.length()+1;
                                    break;
                            }
                        }
                        else{
                            if(style=="divType"){
                                style=getString(where,code);
                                where+=style.length()+1;
                                if(style=="ItemSlot"){
                                    isSlot=true;
                                }
                            }
                        }
                    }
                    this.addSlotFromInfo(new SlotInfo(SlotName,SlotType,xLocaltion,yLocaltion));
                }
            }

        }

        /**
         * 物品栏真正可用
         * @throws NoSuchMethodException 不做表述
         * @throws IllegalAccessException 不做表述
         * @throws InvocationTargetException 不做表述
         * @throws InstantiationException 不做表述
         */
        public void initSlot() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            items = new ItemStackHandler(slotInfos.size()+slots.values().size());
            for(SlotInfo slot : slotInfos){
                if(slot.slotType=="PlayerInventory"){
                    Slot s = slotTypes.get(slot.slotType).getDeclaredConstructor(
                            IInventory.class,Integer.class,Integer.class,Integer.class).newInstance(
                            inv,index++,slot.Location.x,slot.Location.y);
                    slots.put(slot.slotName,s);
                }
                else{
                    Slot s = slotTypes.get(slot.slotType).getDeclaredConstructor(
                            IInventory.class,Integer.class,Integer.class,Integer.class).newInstance(
                            items,index++,slot.Location.x,slot.Location.y);
                    slots.put(slot.slotName,s);
                }
            }

            for(Slot s : slots.values()){
                this.addSlot(s);
            }
        }

        @Override
        public boolean canInteractWith(PlayerEntity playerIn) {
            return true;
        }
    }

    /**
     * @return Container
     */
    public Container getContainer(){
        return this.container;
    }

    public WebScreen(@Nullable ContainerType<?> type, PlayerInventory inv, ITextComponent component)
    {
        super(new WebContainer(type,++id,inv),inv,component);
        viewList = new LinkedList<>();
        rendererList1 = new LinkedList<>();
        rendererList2 = new LinkedList<>();
        shouldCloseOnEsc = true;
    }

    /**
     * 添加一个网页View
     */
    public WebScreen addView(View view)
    {
        viewList.add(view);
        return this;
    }

    /**
     * 设置当按下Esc键时是否关闭GUI
     */
    public WebScreen setShouldCloseOnEsc(boolean shouldCloseOnEsc)
    {
        this.shouldCloseOnEsc = shouldCloseOnEsc;
        return this;
    }

    /**
     * 添加一个Renderer可以在网页渲染前渲染自己的东西
     */
    public WebScreen addPreRenderer(IRenderer renderer)
    {
        rendererList1.add(renderer);
        return this;
    }

    /**
     * 添加一个Renderer可以在网页渲染后渲染自己的东西
     */
    public WebScreen addPostRenderer(IRenderer renderer)
    {
        rendererList2.add(renderer);
        return this;
    }

    protected void init()
    {
        scale = minecraft.getMainWindow().getGuiScaleFactor();
        viewList.forEach(view -> {
            view.enable();
            view.onResize(new Vec2i((int) (width * scale), (int) (height * scale)));
        });
    }

    public void onClose()
    {
        super.onClose();
        viewList.forEach(view -> view.disable());
        WebRenderer.INSTANCE.purgeMemory();
    }

    public boolean shouldCloseOnEsc()
    {
        return shouldCloseOnEsc;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int buttonID)
    {
        mouseX *= scale;
        mouseY *= scale;
        for (View view : viewList)
            view.fireMouseEvent(1, buttonID, (int) mouseX - view.getBounds().x, (int) mouseY - view.getBounds().y);
        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int buttonID)
    {
        mouseX *= scale;
        mouseY *= scale;
        for (View view : viewList)
            view.fireMouseEvent(2, buttonID, (int) mouseX - view.getBounds().x, (int) mouseY - view.getBounds().y);
        return true;
    }

    public void mouseMoved(double mouseX, double mouseY)
    {
        mouseX *= scale;
        mouseY *= scale;
        for (View view : viewList)
            view.fireMouseEvent(0, 0, (int) (mouseX - view.getBounds().x), (int) (mouseY - view.getBounds().y));
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta)
    {
        for (View view : viewList)
        {
            Vec4i vec = view.getBounds();
            if (vec.x <= mouseX && vec.x + vec.w >= mouseX && vec.y <= mouseY && vec.y + vec.h >= mouseY)
                view.fireScrollEvent((int) (scrollDelta * 25));
        }
        return true;
    }

    public boolean charTyped(char codePoint, int modifiers)
    {
        viewList.forEach(view -> view.fireKeyEvent(3, 0, Character.toString(codePoint), 0, 0));
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        int uKeyCode = KeyboardHelper.glfwKeyCodeToUltralightKeyCode(keyCode);
        int uModifiers = KeyboardHelper.glfwModsToUltralightMods(modifiers);
        viewList.forEach(view -> view.fireKeyEvent(2, uModifiers, null, scanCode, uKeyCode));
        if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            charTyped('\r', 0);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        int uKeyCode = KeyboardHelper.glfwKeyCodeToUltralightKeyCode(keyCode);
        int uModifiers = KeyboardHelper.glfwModsToUltralightMods(modifiers);
        viewList.forEach(view -> view.fireKeyEvent(1, uModifiers, null, scanCode, uKeyCode));
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    public void renderBackground(int p_renderBackground_1_)
    {
        RenderSystem.pushMatrix();
        RenderSystem.scaled(scale, scale, scale);
        if (this.minecraft.world != null)
        {
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent(this));
        }
        else
        {
            this.renderDirtBackground(p_renderBackground_1_);
        }
        RenderSystem.popMatrix();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    }

    //double lastTime = 0.0;
    //int fpsSum = 0;

    public void render(int mX, int mY, float pTicks)
    {
        int mouseX = (int) (mX * scale);
        int mouseY = (int) (mY * scale);

        RenderSystem.pushMatrix();
        RenderSystem.scaled(1.0 / scale, 1.0 / scale, 1.0 / scale);

        WebRenderer.INSTANCE.offscreenRender();
        //renderBackground();

        rendererList1.forEach(renderer -> renderer.render(mouseX, mouseY, pTicks));
        viewList.forEach(view -> view.draw());
        rendererList2.forEach(renderer -> renderer.render(mouseX, mouseY, pTicks));

        //fpsSum++;
        //double time = GLFW.glfwGetTime();
        /*if (time - lastTime > 1.0)
        {
            //System.out.printf("FPS = %.1f\n", 1.0 * fpsSum / (time - lastTime));
            int a = (int) (1.0 * fpsSum / (time - lastTime) * 10 + 0.5);
            GLFW.glfwSetWindowTitle(minecraft.getMainWindow().getHandle(),
                    "Minecraft 1.15.2 FPS: " + a / 10 + "." + a % 10);
            lastTime = time;
            fpsSum = 0;
        }*/

        RenderSystem.popMatrix();
    }


}
