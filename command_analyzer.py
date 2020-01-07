from flask import Flask, request, jsonify
from flask_restful import Resource, Api
import socket

app = Flask(__name__)
api = Api(app)

# Liste des opérations valides
# A chaque opération peut correspondre une ou plusieurs commandes vocales
valid_vocal_commands = {
    'play':   ['jouer', 'play', 'lancer'],
    'search': ['chercher', 'cherche', 'search'],
    'pause':  ['pause'],
    'resume': ['reprendre', 'reprend', 'resume'],
    'stop':   ['arrêt', 'arrête', 'arrêter', 'stop'],
    'index':  ['index', 'home'],
}

ip = socket.gethostbyname(socket.gethostname())

class Vocal(Resource):
    # Reçoit une chaîne de caractères en paramètre.
    # Si elle correspond à une opération valide, renvoie cette
    # opération accompagnée des paramètres en JSON
    # Exemple:
    # http://127.0.0.1:5002/command?vocal=jouer%20Morceau%20de%toto
    # => Renvoie {'command': 'play', 'params': 'Morceau de toto'}
    def get(self):
        command = ''
        params = []

        vocal = request.args.get('vocal')
        if vocal:
            vocal = vocal.split(' ')
            # Cherche dans quelle catégorie ('play', 'search', 'pause', ...)
            # la commande vocale appartient.
            for category, commands in valid_vocal_commands.items():
                if vocal[0] in commands:
                    command = category
                    if category in ['play', 'search']:
                        params = ' '.join(x for x in vocal[1:])
                    break

        result = {'command': command, 'params': str(params)}
        return result


api.add_resource(Vocal, '/command')


if __name__ == '__main__':
     app.run(host=str(ip), port='5002')
