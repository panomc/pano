# Pano

Pano is an advanced web platform that comfort your Minecraft server's website needs.

#### Project Status
Pano is still under heavy development. There can be breaking changes but we're trying to keep them as minimum as possible.

#### Prerequisites
JDK 8+ <br>
MySQL  5.0 <br>
Docker (optional) <br>
Docker Compose (optional)

## Development
#### Getting Started
1) Clone this repository.

```bash
git clone --recursive https://gitlab.com/defio-workshop/pano/pano-web-platform.git
cd pano-web-platform
```

##### Compile & Run

```bash
./gradlew vertxRun
```

##### Running Debug with Gradle

```bash
./gradlew vertxDebug
```

### Using Docker (recommended)

```bash
docker-compose up
```

If you would like to run Docker in background, use ` -d` at the end of the command like `docker-compose up -d`. 
If you want to stop Docker while it's running in background `docker-compose down`. 
If you would like to stop Docker while it's not running in background use `ctrl + c`.

##### MySQL Default Values

```bash
database: pano
root password: pano
```

## Contributing
Merge requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
This project is licensed under the GNU GPLv3 License - see the [LICENSE](LICENSE) file for details