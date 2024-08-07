import tkinter as tk
from PIL import Image, ImageTk
from tkinter import filedialog
import win32gui
import win32con
import win32api
import json


def make_window_clickthrough(hwnd):
    ex_style = win32gui.GetWindowLong(hwnd, win32con.GWL_EXSTYLE)
    win32gui.SetWindowLong(hwnd, win32con.GWL_EXSTYLE, ex_style | win32con.WS_EX_LAYERED | win32con.WS_EX_TRANSPARENT)
    win32gui.SetLayeredWindowAttributes(hwnd, win32api.RGB(0, 0, 0), 255, win32con.LWA_COLORKEY)


def create_crosshair_window(image_path, keybind=None):
    global crosshair_root
    if crosshair_root and crosshair_root.state() != 'withdrawn':
        return

    crosshair_root = tk.Toplevel()
    crosshair_root.attributes('-fullscreen', True)
    crosshair_root.attributes('-topmost', True)
    crosshair_root.attributes('-alpha', 0.5)
    crosshair_root.configure(bg='black')

    crosshair_image = Image.open(image_path)
    crosshair_photo = ImageTk.PhotoImage(crosshair_image)

    screen_width = crosshair_root.winfo_screenwidth()
    screen_height = crosshair_root.winfo_screenheight()

    label = tk.Label(crosshair_root, image=crosshair_photo, bg='black')
    label.place(x=(screen_width - crosshair_image.width) // 2, y=(screen_height - crosshair_image.height) // 2)

    crosshair_root.update()
    hwnd = win32gui.FindWindow(None, crosshair_root.title())
    make_window_clickthrough(hwnd)

    def toggle_crosshair(event=None):
        if crosshair_root.state() == 'withdrawn':
            crosshair_root.deiconify()
        else:
            crosshair_root.withdraw()

    toggle_keybind = keybind
    if keybind:
        crosshair_root.bind(f'<{keybind}>', toggle_crosshair)

    crosshair_root.bind('<Button>', lambda e: toggle_crosshair())
    crosshair_root.bind('<Escape>', lambda e: crosshair_root.destroy())




    crosshair_root.protocol("WM_DELETE_WINDOW", on_closing)


def load_keybind():
    try:
        with open('button.json', 'r') as f:
            data = json.load(f)
            return data.get('keybind', None)
    except FileNotFoundError:
        return None


def save_keybind(keybind):
    with open('button.json', 'w') as f:
        json.dump({'keybind': keybind}, f)


def settings_window():
    global settings_root, image_path, keybind_entry, crosshair_root, toggle_keybind

    def upload_image():
        global image_path
        image_path = filedialog.askopenfilename(filetypes=[("Image Files", "*.png;*.jpg;*.jpeg")])
        if image_path:
            print(f"Image uploaded: {image_path}")

    def assign_keybind():
        global toggle_keybind
        keybind = keybind_entry.get()
        if keybind and image_path:
            save_keybind(keybind)
            create_crosshair_window(image_path, keybind)

    def toggle_crosshair_visibility():
        global crosshair_root
        if crosshair_root:
            if crosshair_root.state() != 'withdrawn':
                crosshair_root.withdraw()
            else:
                crosshair_root.deiconify()

    settings_root = tk.Tk()
    settings_root.title("Crosshair Settings")

    tk.Label(settings_root, text="Enter Keybind:").pack(pady=5)
    keybind_entry = tk.Entry(settings_root)
    keybind_entry.pack(pady=10)

    upload_button = tk.Button(settings_root, text="Upload Crosshair Image", command=upload_image)
    upload_button.pack(pady=20)

    keybind_button = tk.Button(settings_root, text="Assign Keybind", command=assign_keybind)
    keybind_button.pack(pady=20)

    toggle_button = tk.Button(settings_root, text="Toggle Crosshair", command=toggle_crosshair_visibility)
    toggle_button.pack(pady=20)


    keybind = load_keybind()
    if keybind:
        keybind_entry.insert(0, keybind)
        if image_path:
            create_crosshair_window(image_path, keybind)

    settings_root.mainloop()


if __name__ == '__main__':
    image_path = None
    crosshair_root = None
    toggle_keybind = load_keybind()


    def toggle_crosshair(event=None):
        global crosshair_root
        if crosshair_root:
            if crosshair_root.state() == 'withdrawn':
                crosshair_root.deiconify()
            else:
                crosshair_root.withdraw()
        else:
            if image_path:
                create_crosshair_window(image_path, toggle_keybind)


    settings_window()


    if toggle_keybind:
        root = tk.Tk()
        root.withdraw()
        root.bind(f'<{toggle_keybind}>', toggle_crosshair)
        root.mainloop()
