data_file = "ticket_data.json"
ticket_channel_id = 1147937285334110299
transcript_channel_id = 1266502642490802186

'''

category_mapping = {
    "â| Support Question": "Support",
    "ð¨ | Unban or Unmute request": "Unban-Unmute",
    "â¼ï¸ | Player Report": "Player-Report",
    "âï¸ | Technical Support": "Technical-Support",
    "âï¸ | Minecraft Question": "Minecraft-Question",
    "ð¨âð¼ | Question": "MC-Question"
}


class TicketDropdown(nextcord.ui.Select):
    def __init__(self):
        options = [
            nextcord.SelectOption(label=display_name)
            for display_name in category_mapping.keys()
        ]
        super().__init__(placeholder="Make a Selection", min_values=1, max_values=1, options=options)

    async def callback(self, interaction: nextcord.Interaction):
        display_name = self.values[0]
        actual_category_name = category_mapping[display_name]
        await interaction.response.send_modal(TicketModal(display_name, actual_category_name))
        await interaction.message.edit(view=self.view)


class TicketModal(nextcord.ui.Modal):
    def __init__(self, display_name: str, actual_category_name: str):
        self.display_name = display_name
        self.actual_category_name = actual_category_name
        super().__init__(title=f"{display_name} Ticket", timeout=None) 
        self.add_fields()

    def add_fields(self):
        if self.actual_category_name == "Player-Report":
            self.add_item(
                nextcord.ui.TextInput(label="What is your Minecraft name?", style=nextcord.TextInputStyle.short,
                                      required=True))
            self.add_item(nextcord.ui.TextInput(label="Which player would you like to report?",
                                                style=nextcord.TextInputStyle.short, required=True))
            self.add_item(nextcord.ui.TextInput(label="Why are you reporting this player?",
                                                style=nextcord.TextInputStyle.paragraph, required=True))
        elif self.actual_category_name == "Unban-Unmute":
            self.add_item(
                nextcord.ui.TextInput(label="What is your Minecraft name?", style=nextcord.TextInputStyle.short,
                                      required=True))
            self.add_item(
                nextcord.ui.TextInput(label="In which game mode were you banned?", style=nextcord.TextInputStyle.short,
                                      required=True))
            self.add_item(
                nextcord.ui.TextInput(label="What is the ban reason?", style=nextcord.TextInputStyle.paragraph,
                                      required=True))
            self.add_item(
                nextcord.ui.TextInput(label="Describe your situation", style=nextcord.TextInputStyle.paragraph,
                                      required=True))
        elif self.actual_category_name == "MC-Question":
            self.add_item(nextcord.ui.TextInput(label="What is your username?", style=nextcord.TextInputStyle.short,
                                                required=True))
            self.add_item(nextcord.ui.TextInput(label="What is your question regarding MC?",
                                                style=nextcord.TextInputStyle.paragraph, required=True))
        elif self.actual_category_name == "Technical-Support":
            self.add_item(nextcord.ui.TextInput(label="Username", style=nextcord.TextInputStyle.short, required=True))
            self.add_item(nextcord.ui.TextInput(label="Describe your problem", style=nextcord.TextInputStyle.paragraph,
                                                required=True))
        else:
            self.add_item(nextcord.ui.TextInput(label="Describe your problem", style=nextcord.TextInputStyle.paragraph,
                                                required=True))

    async def callback(self, interaction: nextcord.Interaction):
        description = "\n".join([f"{child.label}: {child.value}" for child in self.children])
        ticket_channel = await create_ticket_channel(interaction.guild, interaction.user, self.actual_category_name,
                                                     description)
        await interaction.response.send_message(f"Your ticket got created, available here {ticket_channel.mention}",ephemeral=True)


class CloseTicketButton(nextcord.ui.Button):
    def __init__(self):
        super().__init__(label="Close Ticket", style=nextcord.ButtonStyle.danger)

    async def callback(self, interaction: nextcord.Interaction):
        await generate_transcript(interaction.channel)
        await interaction.channel.delete()


class CloseTicketView(nextcord.ui.View):
    def __init__(self):
        super().__init__()
        self.add_item(CloseTicketButton())


async def create_ticket_channel(guild, user, category_name, description):
    overwrites = {
        guild.default_role: nextcord.PermissionOverwrite(read_messages=False),
        user: nextcord.PermissionOverwrite(read_messages=True, send_messages=True),
    }
    category_channel = nextcord.utils.get(guild.categories, name=category_name)
    if not category_channel:
        category_channel = await guild.create_category(category_name)

    ticket_channel = await category_channel.create_text_channel(f"Ticket-{user.name}", overwrites=overwrites)
    embed = nextcord.Embed(title=f"{category_name} Ticket", description=description)
    await ticket_channel.send(f"{user.mention}, Ticket got created!", embed=embed, view=CloseTicketView())
    return ticket_channel


class TicketDropdownView(nextcord.ui.View):
    def __init__(self):
        super().__init__(timeout=None)
        self.add_item(TicketDropdown())


async def generate_transcript(channel):
    messages = await channel.history(limit=None).flatten()
    transcript = "\n".join([f"{message.author}: {message.content}" for message in messages[::-1]])

    with open("transcript.txt", "w", encoding="utf-8") as f:
        f.write(transcript)

    transcript_channel = bot.get_channel(transcript_channel_id)
    if transcript_channel:
        await transcript_channel.send(file=nextcord.File("transcript.txt"))




@bot.slash_command(name='ticket', description='Create a ticket')
async def ticket(interaction: nextcord.Interaction):
    if not interaction.user.guild_permissions.administrator:
        await interaction.response.send_message("Permission leak.", ephemeral=True)
        return

    embed = nextcord.Embed(title="SERVERNAME.net Â» Support", description="Press the button below regarding to your issue.")
    await interaction.response.send_message(embed=embed, view=TicketDropdownView())
    save_data(interaction.channel_id)




'''