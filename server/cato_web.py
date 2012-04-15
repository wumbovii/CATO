import web
import audio
from pymongo import Connection

connection = Connection('mongodb://admin:sync2200@staff.mongohq.com:10068/synced')
db = connection['synced']
config = db['config'].find_one()
idx_count = config['idx_count']
tracks = db['mixes']

urls = (
    '/', 'index',
    '/tag/(.+)', 'tag'
)

class index:
    def GET(self):
        print "index access"
        return "hello world!"
    
    def POST(self):
        return "hello world!"

class tag:
    def GET(self, name):
        return "hello world " + str(name)
    
    def POST(self, name):
        destination_path = "uploads/test_track.mp3"
        data = web.input()
               
        fout = open(destination_path, 'w')
        fout.write(data['uploadedfile'])
        fout.close()
        
        result = audio.classify(destination_path)
        web.debug(result)
        return result[0][1].split('_')[0]

if __name__ == "__main__":
    app = web.application(urls, globals())
    app.internalerror = web.debugerror
    app.run()