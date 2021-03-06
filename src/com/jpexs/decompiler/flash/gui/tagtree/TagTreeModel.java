/*
 *  Copyright (C) 2010-2015 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.tagtree;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFContainerItem;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.TreeNodeType;
import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ASMSourceContainer;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.timeline.AS2Package;
import com.jpexs.decompiler.flash.timeline.AS3Package;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.FrameScript;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.flash.treeitems.FolderItem;
import com.jpexs.decompiler.flash.treeitems.HeaderItem;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class TagTreeModel implements TreeModel {

    public static final String FOLDER_SHAPES = "shapes";

    public static final String FOLDER_MORPHSHAPES = "morphshapes";

    public static final String FOLDER_SPRITES = "sprites";

    public static final String FOLDER_TEXTS = "texts";

    public static final String FOLDER_IMAGES = "images";

    public static final String FOLDER_MOVIES = "movies";

    public static final String FOLDER_SOUNDS = "sounds";

    public static final String FOLDER_BUTTONS = "buttons";

    public static final String FOLDER_FONTS = "fonts";

    public static final String FOLDER_BINARY_DATA = "binaryData";

    public static final String FOLDER_FRAMES = "frames";

    public static final String FOLDER_OTHERS = "others";

    public static final String FOLDER_SCRIPTS = "scripts";

    private final TagTreeRoot root = new TagTreeRoot();

    private final List<SWFContainerItem> swfs;

    private final Map<SWF, List<TreeItem>> swfFolders;

    private final boolean addAllFolders;

    private final Map<Tag, TagScript> tagScriptCache;

    public TagTreeModel(List<SWFList> swfs, boolean addAllFolders) {
        this.swfs = new ArrayList<>();
        this.swfFolders = new HashMap<>();
        this.addAllFolders = addAllFolders;
        this.tagScriptCache = new HashMap<>();
        Main.startWork(AppStrings.translate("work.buildingscripttree") + "...");
        for (SWFList swfList : swfs) {
            if (swfList.bundle != null) {
                this.swfs.add(swfList);
                for (SWF swf : swfList) {
                    createTagList(swf);
                }
            } else {
                SWF swf = swfList.get(0);
                this.swfs.add(swf);
                createTagList(swf);
            }
        }
        Main.stopWork();
    }

    private String translate(String key) {
        return AppStrings.translate(key);
    }

    private List<SoundStreamHeadTypeTag> getSoundStreams(DefineSpriteTag sprite) {
        List<SoundStreamHeadTypeTag> ret = new ArrayList<>();
        for (Tag t : sprite.subTags) {
            if (t instanceof SoundStreamHeadTypeTag) {
                ret.add((SoundStreamHeadTypeTag) t);
            }
        }
        return ret;
    }

    private void createTagList(SWF swf) {
        List<TreeItem> nodeList = new ArrayList<>();
        List<TreeItem> frames = new ArrayList<>();
        List<TreeItem> shapes = new ArrayList<>();
        List<TreeItem> morphShapes = new ArrayList<>();
        List<TreeItem> sprites = new ArrayList<>();
        List<TreeItem> buttons = new ArrayList<>();
        List<TreeItem> images = new ArrayList<>();
        List<TreeItem> fonts = new ArrayList<>();
        List<TreeItem> texts = new ArrayList<>();
        List<TreeItem> movies = new ArrayList<>();
        List<TreeItem> sounds = new ArrayList<>();
        List<TreeItem> binaryData = new ArrayList<>();
        List<TreeItem> others = new ArrayList<>();

        for (Tag t : swf.tags) {
            TreeNodeType ttype = TagTree.getTreeNodeType(t);
            switch (ttype) {
                case SHAPE:
                    shapes.add(t);
                    break;
                case MORPH_SHAPE:
                    morphShapes.add(t);
                    break;
                case SPRITE:
                    sprites.add(t);
                    sounds.addAll(getSoundStreams((DefineSpriteTag) t));
                    break;
                case BUTTON:
                    buttons.add(t);
                    break;
                case IMAGE:
                    images.add(t);
                    break;
                case FONT:
                    fonts.add(t);
                    break;
                case TEXT:
                    texts.add(t);
                    break;
                case MOVIE:
                    movies.add(t);
                    break;
                case SOUND:
                    sounds.add(t);
                    break;
                case BINARY_DATA:
                    binaryData.add(t);
                    break;
                case AS:
                    break;
                default:
                    if (t.getId() != ShowFrameTag.ID && !ShowFrameTag.isNestedTagType(t.getId())) {
                        others.add(t);
                    }
                    break;
            }
        }

        Timeline timeline = swf.getTimeline();
        int frameCount = timeline.getFrameCount();
        for (int i = 0; i < frameCount; i++) {
            frames.add(timeline.getFrame(i));
        }

        for (int i = sounds.size() - 1; i >= 0; i--) {
            TreeItem sound = sounds.get(i);
            if (sound instanceof SoundStreamHeadTypeTag) {
                List<SoundStreamBlockTag> blocks = ((SoundStreamHeadTypeTag) sound).getBlocks();
                if (blocks.isEmpty()) {
                    sounds.remove(i);
                }
            }
        }

        nodeList.add(new HeaderItem(swf, translate("node.header")));

        if (addAllFolders || !shapes.isEmpty()) {
            FolderItem shapesNode = new FolderItem(translate("node.shapes"), FOLDER_SHAPES, swf, shapes);
            nodeList.add(shapesNode);
        }
        if (addAllFolders || !morphShapes.isEmpty()) {
            FolderItem morphShapesNode = new FolderItem(translate("node.morphshapes"), FOLDER_MORPHSHAPES, swf, morphShapes);
            nodeList.add(morphShapesNode);
        }
        if (addAllFolders || !sprites.isEmpty()) {
            FolderItem spritesNode = new FolderItem(translate("node.sprites"), FOLDER_SPRITES, swf, sprites);
            nodeList.add(spritesNode);
        }
        if (addAllFolders || !texts.isEmpty()) {
            FolderItem textsNode = new FolderItem(translate("node.texts"), FOLDER_TEXTS, swf, texts);
            nodeList.add(textsNode);
        }
        if (addAllFolders || !images.isEmpty()) {
            FolderItem imagesNode = new FolderItem(translate("node.images"), FOLDER_IMAGES, swf, images);
            nodeList.add(imagesNode);
        }
        if (addAllFolders || !movies.isEmpty()) {
            FolderItem moviesNode = new FolderItem(translate("node.movies"), FOLDER_MOVIES, swf, movies);
            nodeList.add(moviesNode);
        }
        if (addAllFolders || !sounds.isEmpty()) {
            FolderItem soundsNode = new FolderItem(translate("node.sounds"), FOLDER_SOUNDS, swf, sounds);
            nodeList.add(soundsNode);
        }
        if (addAllFolders || !buttons.isEmpty()) {
            FolderItem buttonsNode = new FolderItem(translate("node.buttons"), FOLDER_BUTTONS, swf, buttons);
            nodeList.add(buttonsNode);
        }
        if (addAllFolders || !fonts.isEmpty()) {
            FolderItem fontsNode = new FolderItem(translate("node.fonts"), FOLDER_FONTS, swf, fonts);
            nodeList.add(fontsNode);
        }
        if (addAllFolders || !binaryData.isEmpty()) {
            FolderItem binaryDataNode = new FolderItem(translate("node.binaryData"), FOLDER_BINARY_DATA, swf, binaryData);
            nodeList.add(binaryDataNode);
        }
        if (!frames.isEmpty()) {
            FolderItem framesNode = new FolderItem(translate("node.frames"), FOLDER_FRAMES, swf, frames);
            nodeList.add(framesNode);
        }
        if (!others.isEmpty()) {
            FolderItem otherNode = new FolderItem(translate("node.others"), FOLDER_OTHERS, swf, others);
            nodeList.add(otherNode);
        }

        if (swf.isAS3()) {
            if (!swf.getAbcList().isEmpty()) {
                nodeList.add(new ClassesListTreeModel(swf));
            }
        } else {
            List<TreeItem> subNodes = swf.getFirstLevelASMNodes(tagScriptCache);

            if (subNodes.size() > 0) {
                TreeItem actionScriptNode = new FolderItem(translate("node.scripts"), FOLDER_SCRIPTS, swf, subNodes);
                nodeList.add(actionScriptNode);
            }
        }

        swfFolders.put(swf, nodeList);
    }

    public TreeItem getScriptsNode(SWF swf) {
        int childCount = getChildCount(swf);
        for (int i = 0; i < childCount; i++) {
            TreeItem child = getChild(swf, i);
            if (child instanceof ClassesListTreeModel) {
                return child;
            } else if (child instanceof FolderItem) {
                FolderItem folder = (FolderItem) child;
                if (folder.getName().equals(FOLDER_SCRIPTS)) {
                    return folder;
                }
            }
        }

        return null;
    }

    public TreeItem getFolderNode(SWF swf, String folderType) {
        int childCount = getChildCount(swf);
        for (int i = 0; i < childCount; i++) {
            TreeItem child = getChild(swf, i);
            if (child instanceof FolderItem) {
                FolderItem folder = (FolderItem) child;
                if (folder.getName().equals(folderType)) {
                    return folder;
                }
            }
        }

        return null;
    }

    private Frame searchForFrame(Object parent, SWF swf, Timelined t, int frame) {
        int childCount = getChildCount(parent);
        Frame lastVisibleFrame = null;
        for (int i = 0; i < childCount; i++) {
            TreeItem child = getChild(parent, i);
            if ((child instanceof DefineSpriteTag) && child == t) {
                Frame si = searchForFrame(child, swf, t, frame);
                if (si != null) {
                    return si;
                }
            }
            if (child instanceof Frame) {
                Frame f = (Frame) child;
                if (f.frame <= frame) {
                    lastVisibleFrame = f;
                }
            }
            if (child instanceof FolderItem) {
                FolderItem folder = (FolderItem) child;
                if (folder.getName().equals(FOLDER_FRAMES) && t == swf) {
                    Frame si = searchForFrame(folder, swf, t, frame);
                    if (si != null) {
                        return si;
                    }
                }
                if (folder.getName().equals(FOLDER_SPRITES)) {
                    Frame si = searchForFrame(folder, swf, t, frame);
                    if (si != null) {
                        return si;
                    }
                }
            }
        }
        return lastVisibleFrame;
    }

    public Frame getFrame(SWF swf, Timelined t, int frame) {
        return searchForFrame(swf, swf, t, frame);
    }

    private List<TreeItem> searchTag(TreeItem obj, TreeItem parent, List<TreeItem> path) {
        List<TreeItem> ret = null;
        int cnt = getChildCount(parent);
        for (int i = 0; i < cnt; i++) {
            TreeItem n = getChild(parent, i);
            List<TreeItem> newPath = new ArrayList<>();
            newPath.addAll(path);
            newPath.add(n);

            if (n instanceof AS3ClassTreeItem) {
                AS3ClassTreeItem te = (AS3ClassTreeItem) n;
                if (obj.equals(te)) {
                    return newPath;
                }
            }

            if (obj instanceof FolderItem && n instanceof FolderItem) {
                // FolderItems are always recreated, so compare them by name
                FolderItem nds = (FolderItem) n;
                FolderItem objs = (FolderItem) obj;
                if (objs.getName().equals(nds.getName())) {
                    return newPath;
                }
            } else {
                TreeItem n2 = n;
                if (n2 instanceof TagScript) {
                    n2 = ((TagScript) n2).getTag();
                }

                if (obj.equals(n2)) {
                    return newPath;
                }
            }

            ret = searchTag(obj, n, newPath);
            if (ret != null) {
                return ret;
            }
        }
        return ret;
    }

    public TreePath getTagPath(TreeItem obj) {
        List<TreeItem> path = new ArrayList<>();
        path.add(getRoot());
        path = searchTag(obj, getRoot(), path);
        if (path == null) {
            return null;
        }
        TreePath tp = new TreePath(path.toArray(new Object[path.size()]));
        return tp;
    }

    @Override
    public TreeItem getRoot() {
        return root;
    }

    private List<TreeItem> getSwfFolders(SWF swf) {
        List<TreeItem> ret = swfFolders.get(swf);
        if (ret == null) {
            createTagList(swf);
            ret = swfFolders.get(swf);
        }

        return ret;
    }

    @Override
    public TreeItem getChild(Object parent, int index) {
        TreeItem parentNode = (TreeItem) parent;
        if (parentNode == root) {
            return swfs.get(index);
        } else if (parentNode instanceof SWFList) {
            return ((SWFList) parentNode).swfs.get(index);
        } else if (parentNode instanceof SWF) {
            return getSwfFolders((SWF) parentNode).get(index);
        } else if (parentNode instanceof FolderItem) {
            return ((FolderItem) parentNode).subItems.get(index);
        } else if (parentNode instanceof Frame) {
            return ((Frame) parentNode).innerTags.get(index);
        } else if (parentNode instanceof DefineSpriteTag) {
            return ((DefineSpriteTag) parentNode).getTimeline().getFrame(index);
        } else if (parentNode instanceof DefineBinaryDataTag) {
            return ((DefineBinaryDataTag) parentNode).innerSwf;
        } else if (parentNode instanceof AS2Package) {
            return ((AS2Package) parentNode).getChild(index);
        } else if (parentNode instanceof FrameScript) {
            Frame parentFrame = ((FrameScript) parentNode).getFrame();
            TreeItem result;
            if (index < parentFrame.actionContainers.size()) {
                result = parentFrame.actionContainers.get(index);
            } else {
                index -= parentFrame.actionContainers.size();
                result = parentFrame.actions.get(index);
            }
            if (result instanceof Tag) {
                Tag resultTag = (Tag) result;
                TagScript tagScript = tagScriptCache.get(resultTag);
                if (tagScript == null) {
                    List<TreeItem> subNodes = new ArrayList<>();
                    if (result instanceof ASMSourceContainer) {
                        for (ASMSource item : ((ASMSourceContainer) result).getSubItems()) {
                            subNodes.add(item);
                        }
                    }
                    tagScript = new TagScript(result.getSwf(), resultTag, subNodes);
                    tagScriptCache.put(resultTag, tagScript);
                }
                result = tagScript;
            }
            return result;
        } else if (parentNode instanceof TagScript) {
            return ((TagScript) parentNode).getFrames().get(index);
        } else if (parentNode instanceof ClassesListTreeModel) {
            ClassesListTreeModel clt = (ClassesListTreeModel) parentNode;
            return clt.getChild(clt.getRoot(), index);
        } else if (parentNode instanceof AS3ClassTreeItem) {
            return ((AS3Package) parentNode).getChild(index);
        }

        throw new Error("Unsupported parent type: " + parentNode.getClass().getName());
    }

    @Override
    public int getChildCount(Object parent) {
        TreeItem parentNode = (TreeItem) parent;
        if (parentNode == root) {
            return swfs.size();
        } else if (parentNode instanceof SWFList) {
            return ((SWFList) parentNode).swfs.size();
        } else if (parentNode instanceof SWF) {
            return getSwfFolders((SWF) parentNode).size();
        } else if (parentNode instanceof HeaderItem) {
            return 0;
        } else if (parentNode instanceof FolderItem) {
            return ((FolderItem) parentNode).subItems.size();
        } else if (parentNode instanceof Frame) {
            return ((Frame) parentNode).innerTags.size();
        } else if (parentNode instanceof DefineSpriteTag) {
            return ((DefineSpriteTag) parentNode).getTimeline().getFrameCount();
        } else if (parentNode instanceof DefineBinaryDataTag) {
            return ((DefineBinaryDataTag) parentNode).innerSwf == null ? 0 : 1;
        } else if (parentNode instanceof AS2Package) {
            return ((AS2Package) parentNode).getChildCount();
        } else if (parentNode instanceof FrameScript) {
            Frame parentFrame = ((FrameScript) parentNode).getFrame();
            return parentFrame.actionContainers.size() + parentFrame.actions.size();
        } else if (parentNode instanceof TagScript) {
            return ((TagScript) parentNode).getFrames().size();
        } else if (parentNode instanceof ClassesListTreeModel) {
            ClassesListTreeModel clt = (ClassesListTreeModel) parentNode;
            return clt.getChildCount(clt.getRoot());
        } else if (parentNode instanceof AS3Package) {
            return ((AS3Package) parentNode).getChildCount();
        }

        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return (getChildCount(node) == 0);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        TreeItem parentNode = (TreeItem) parent;
        TreeItem childNode = (TreeItem) child;
        if (parentNode == root) {
            return swfs.indexOf(childNode);
        } else if (parentNode instanceof SWFList) {
            return ((SWFList) parentNode).swfs.indexOf(childNode);
        } else if (parentNode instanceof SWF) {
            return getSwfFolders((SWF) parentNode).indexOf(childNode);
        } else if (parentNode instanceof FolderItem) {
            return ((FolderItem) parentNode).subItems.indexOf(childNode);
        } else if (parentNode instanceof Frame) {
            return ((Frame) parentNode).innerTags.indexOf(childNode);
        } else if (parentNode instanceof DefineSpriteTag) {
            return ((Frame) childNode).frame;
        } else if (parentNode instanceof DefineBinaryDataTag) {
            return 0; // binary data tag can have only 1 child
        } else if (parentNode instanceof AS2Package) {
            return ((AS2Package) parentNode).getIndexOfChild(childNode);
        } else if (parentNode instanceof FrameScript) {
            Frame parentFrame = ((FrameScript) parentNode).getFrame();
            if (childNode instanceof TagScript) {
                childNode = ((TagScript) childNode).getTag();
            }
            if (childNode instanceof ASMSourceContainer) {
                return parentFrame.actionContainers.indexOf(childNode);
            } else {
                return parentFrame.actionContainers.size() + parentFrame.actions.indexOf(childNode);
            }
        } else if (parentNode instanceof TagScript) {
            return ((TagScript) parentNode).getFrames().indexOf(childNode);
        } else if (parentNode instanceof ClassesListTreeModel) {
            ClassesListTreeModel clt = (ClassesListTreeModel) parentNode;
            return clt.getIndexOfChild(clt.getRoot(), childNode);
        } else if (parentNode instanceof AS3ClassTreeItem) {
            return ((AS3Package) parentNode).getIndexOfChild((AS3ClassTreeItem) childNode);
        }

        throw new Error("Unsupported parent type: " + parentNode.getClass().getName());
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }
}
