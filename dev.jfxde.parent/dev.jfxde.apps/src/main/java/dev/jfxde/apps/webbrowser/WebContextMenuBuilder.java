package dev.jfxde.apps.webbrowser;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import netscape.javascript.JSObject;

public class WebContextMenuBuilder {

	private JSObject jsobject;
	private WebPageView view;

	private WebContextMenuBuilder(WebPageView view, JSObject jsobject) {
		this.view = view;
		this.jsobject = jsobject;
	}

	public static void register(WebPageView view) {
		view.getWebView().setContextMenuEnabled(false);
		// use filter with pressed because second yahoo search page clears
		// selection when mouse clicked.
		view.getWebView().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {

			if (e.getButton() == MouseButton.SECONDARY) {

				JSObject jsobject = JSUtils.getElementFromPoint(view.getWebView().getEngine(), e.getX(), e.getY());

				ContextMenu contextMenu = WebContextMenuBuilder.get(view, jsobject).build();
				if (!contextMenu.getItems().isEmpty()) {
					contextMenu.show(view.getScene().getWindow(), e.getScreenX(), e.getScreenY());
				}
			}
		});
	}

	private static WebContextMenuBuilder get(WebPageView view, JSObject jsobject) {
		WebContextMenuBuilder builder = new WebContextMenuBuilder(view, jsobject);

		return builder;
	}

	public ContextMenu build() {

		ContextMenu contextMenu = new ContextMenu();
		contextMenu.setAutoHide(true);

		JSObject linkJsobject = JSUtils.getJSObject(jsobject, "a");

		if (linkJsobject != null) {
			addLinkContextMenu(contextMenu, linkJsobject);
		}

        String selectedText = JSUtils.getSelection(view.getWebView().getEngine());

        if (selectedText != null && !selectedText.isEmpty()) {
            if (!contextMenu.getItems().isEmpty()) {
                contextMenu.getItems().add(new SeparatorMenuItem());
            }
            addCopyContextMenu(contextMenu, selectedText);
        }

		return contextMenu;
	}

	private void addLinkContextMenu(ContextMenu menu, JSObject jsobject) {

		String url = (String) jsobject.getMember("href");

		MenuItem linkInWindow = new MenuItem();
		linkInWindow.textProperty().bind(view.getContext().rc().getTextBinding("linkInNewTab"));
		linkInWindow.setOnAction(e -> view.setLinkInNewTab(url));

		MenuItem copyLink = new MenuItem();
		copyLink.textProperty().bind(view.getContext().rc().getTextBinding("linkCopy"));
		copyLink.setOnAction(e -> {

			Clipboard clipboard = Clipboard.getSystemClipboard();
			ClipboardContent content = new ClipboardContent();
			content.putString(url);
			clipboard.setContent(content);
		});

		menu.getItems().addAll(linkInWindow, copyLink);
	}

    private void addCopyContextMenu(ContextMenu menu, String selectedText) {
        MenuItem copy = new MenuItem();
        copy.textProperty().bind(view.getContext().rc().getTextBinding("copy"));
        copy.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedText);
            clipboard.setContent(content);

        });

        MenuItem searchFor = new MenuItem();
        searchFor.setText(view.getContext().rc().getTextMaxWidth("searchFor", selectedText, 15));
        searchFor.setOnAction(e -> view.setLinkInNewTab(view.getSearchEngine().getQueryUrl(selectedText)));

        menu.getItems().addAll(copy, searchFor);
    }
}
